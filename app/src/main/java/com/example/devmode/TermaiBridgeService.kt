package com.example.devmode

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.bridge.ConversationMirror
import com.example.bridge.HttpBridgeServer
import com.example.bridge.OpenAiCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Estado da ponte, observado pela tela de modo desenvolvedor. */
object BridgeStatus {
    private val _state = MutableStateFlow("parada")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    internal fun setState(value: String) {
        _state.value = value
    }

    internal fun setResult(value: String) {
        _result.value = value
    }
}

/**
 * Ponte para o TermAI (F4 + API compatível): um servidor local em 127.0.0.1 recebe chamadas
 * no formato de chat completions, leva o que é novo ao chat do site num WebView sem tela e
 * devolve a resposta separada em texto e chamadas de ferramenta.
 * Roda como serviço em primeiro plano, com notificação.
 */
class TermaiBridgeService : Service() {

    private val main = Handler(Looper.getMainLooper())
    private val lock = Any()
    private val mirror = ConversationMirror()
    private val pageReady = LinkedBlockingQueue<Boolean>()
    private val cycleEvents = LinkedBlockingQueue<JSONObject>()
    private var webView: WebView? = null
    private var server: HttpBridgeServer? = null
    private var profile: SiteProfile? = null
    private var callCounter = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startBridge(intent)
            ACTION_SEND -> sendOnce(intent)
            ACTION_STOP -> stopAll("ponte parada")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        server?.stop()
        server = null
        main.post { releaseWebView() }
        super.onDestroy()
    }

    private fun startBridge(intent: Intent) {
        val p = readProfile(intent)
        val token = intent.getStringExtra(EXTRA_TOKEN).orEmpty()
        val port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT)
        if (p == null || token.isBlank()) {
            stopAll("dados inválidos")
            return
        }
        createChannel()
        startForeground(NOTIFICATION_ID, notification("iniciando ponte"))
        profile = p
        server?.stop()
        val s = HttpBridgeServer(port, token) { method, path, body -> route(method, path, body) }
        try {
            s.start()
            server = s
            setStatus("ponte ativa em 127.0.0.1:" + port)
        } catch (e: Exception) {
            stopAll("porta " + port + " indisponível")
        }
    }

    private fun route(method: String, path: String, body: String): Pair<Int, String> {
        return when {
            method == "GET" && path == "/health" -> 200 to "{\"ok\":true}"
            method == "POST" && path == "/v1/chat/completions" -> chat(body)
            else -> 404 to "{\"error\":\"rota não encontrada\"}"
        }
    }

    private fun chat(body: String): Pair<Int, String> {
        synchronized(lock) {
            val json = runCatching { JSONObject(body) }.getOrNull()
                ?: return 400 to errorJson("corpo inválido")
            if (json.optBoolean("stream", false)) {
                return 400 to errorJson("streaming não suportado nesta ponte")
            }
            val p = profile ?: return 500 to errorJson("perfil não configurado")
            val turns = OpenAiCompat.parseMessages(json)
            val plan = mirror.plan(turns)
            val text = mirror.render(plan)
            if (plan.newChat && !openChat(p.url)) {
                return 504 to errorJson("o site não carregou")
            }
            val raw = sendAndWait(p, text)
                ?: return 504 to errorJson("a resposta do site não terminou a tempo")
            callCounter++
            val reply = OpenAiCompat.parseReply(raw, json.optJSONArray("tools"), callCounter.toString())
            mirror.commit(turns, reply)
            val out = OpenAiCompat.completion(json.optString("model", "navegador"), reply, text.length)
            return 200 to out.toString()
        }
    }

    private fun errorJson(message: String): String =
        JSONObject().put("error", JSONObject().put("message", message)).toString()

    /** Abre um chat novo: carrega a página e espera ela montar a interface. */
    private fun openChat(url: String): Boolean {
        pageReady.clear()
        cycleEvents.clear()
        main.post {
            ensureWebView()
            webView?.loadUrl(url)
        }
        val ready = pageReady.poll(60, TimeUnit.SECONDS) != null
        if (ready) {
            Thread.sleep(1500)
        }
        return ready
    }

    /** Envia o texto ao chat e espera o fim da resposta. Devolve o texto bruto ou null se falhar. */
    private fun sendAndWait(p: SiteProfile, prompt: String): String? {
        cycleEvents.clear()
        main.post {
            webView?.evaluateJavascript(DomScripts.sendScript(p, prompt, TIMEOUT_MS), null)
        }
        val deadline = System.currentTimeMillis() + TIMEOUT_MS + 30_000L
        while (true) {
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0) return null
            val event = cycleEvents.poll(remaining, TimeUnit.MILLISECONDS) ?: return null
            when (event.optString("state")) {
                "sending" -> setStatus("aguardando resposta")
                "done" -> return event.optString("text")
                "timeout" -> return null
                "error" -> {
                    setStatus("erro: " + event.optString("message"))
                    return null
                }
            }
        }
    }

    /** Teste único (botão da tela de desenvolvedor): abre o chat, envia o prompt e mostra a resposta. */
    private fun sendOnce(intent: Intent) {
        val p = readProfile(intent)
        val prompt = intent.getStringExtra(EXTRA_PROMPT).orEmpty()
        if (p == null || prompt.isBlank()) {
            stopAll("dados inválidos")
            return
        }
        createChannel()
        startForeground(NOTIFICATION_ID, notification("enviando teste"))
        profile = p
        Thread {
            var raw: String? = null
            synchronized(lock) {
                raw = if (openChat(p.url)) sendAndWait(p, prompt) else null
            }
            BridgeStatus.setResult(raw.orEmpty())
            BridgeStatus.setState(if (raw != null) "resposta recebida" else "falhou")
            if (server == null) {
                main.post {
                    releaseWebView()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }.start()
    }

    private fun ensureWebView() {
        if (webView != null) return
        val view = WebView(this)
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.addJavascriptInterface(
            DevBridge(
                onPicked = { },
                onCycleEvent = { json ->
                    cycleEvents.offer(runCatching { JSONObject(json) }.getOrDefault(JSONObject()))
                }
            ),
            "TermavigatorDev"
        )
        view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(v: WebView?, url: String?) {
                pageReady.offer(true)
            }
        }
        // Dá tamanho ao WebView sem janela, para o site montar o layout
        view.layout(0, 0, 1080, 1920)
        webView = view
    }

    private fun stopAll(message: String) {
        server?.stop()
        server = null
        mirror.reset()
        BridgeStatus.setState(message)
        main.post { releaseWebView() }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseWebView() {
        webView?.apply {
            stopLoading()
            removeJavascriptInterface("TermavigatorDev")
            destroy()
        }
        webView = null
    }

    private fun readProfile(intent: Intent): SiteProfile? {
        return runCatching {
            SiteProfile.fromJson(JSONObject(intent.getStringExtra(EXTRA_PROFILE).orEmpty()))
        }.getOrNull()
    }

    private fun setStatus(text: String) {
        BridgeStatus.setState(text)
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(text))
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Ponte com a IA", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun notification(text: String): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TermaiBridgeService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Termavigator · ponte com a IA")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "Parar", stopIntent)
            .build()
    }

    companion object {
        const val ACTION_START = "com.example.devmode.START"
        const val ACTION_SEND = "com.example.devmode.SEND"
        const val ACTION_STOP = "com.example.devmode.STOP"
        const val EXTRA_PROFILE = "profile"
        const val EXTRA_PROMPT = "prompt"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_PORT = "port"
        const val DEFAULT_PORT = 8787
        private const val TIMEOUT_MS = 600_000
        private const val CHANNEL_ID = "termai_bridge"
        private const val NOTIFICATION_ID = 4101
        private const val PREFS = "termai_bridge"

        /** Teste único em segundo plano. */
        fun send(context: Context, profile: SiteProfile, prompt: String) {
            val intent = Intent(context, TermaiBridgeService::class.java)
                .setAction(ACTION_SEND)
                .putExtra(EXTRA_PROFILE, profile.toJson().toString())
                .putExtra(EXTRA_PROMPT, prompt)
            ContextCompat.startForegroundService(context, intent)
        }

        /** Liga a API local para o TermAI, com o token deste aparelho. */
        fun startBridge(context: Context, profile: SiteProfile, port: Int = DEFAULT_PORT) {
            val intent = Intent(context, TermaiBridgeService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_PROFILE, profile.toJson().toString())
                .putExtra(EXTRA_TOKEN, bridgeToken(context))
                .putExtra(EXTRA_PORT, port)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopBridge(context: Context) {
            context.startService(Intent(context, TermaiBridgeService::class.java).setAction(ACTION_STOP))
        }

        /** Token da ponte: gerado uma vez e guardado no aparelho. */
        fun bridgeToken(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs.getString("token", null)?.let { return it }
            val token = UUID.randomUUID().toString().replace("-", "")
            prefs.edit().putString("token", token).apply()
            return token
        }
    }
}
