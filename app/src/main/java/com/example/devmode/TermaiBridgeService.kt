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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/** Estado da ponte em segundo plano, observado pela tela de modo desenvolvedor. */
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
 * Ponte em segundo plano (F4): executa um envio de teste num WebView sem tela,
 * dentro de um serviço em primeiro plano com notificação. O WebView existe só durante a tarefa.
 */
class TermaiBridgeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null
    private var sent = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SEND -> startTask(intent)
            ACTION_STOP -> finishTask("parada pelo usuário")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWebView()
        super.onDestroy()
    }

    private fun startTask(intent: Intent) {
        val profile = runCatching {
            SiteProfile.fromJson(JSONObject(intent.getStringExtra(EXTRA_PROFILE).orEmpty()))
        }.getOrNull()
        val prompt = intent.getStringExtra(EXTRA_PROMPT).orEmpty()
        if (profile == null || prompt.isBlank()) {
            finishTask("dados inválidos")
            return
        }

        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification("preparando"))
        BridgeStatus.setState("preparando")
        BridgeStatus.setResult("")
        sent = false

        handler.post {
            releaseWebView()
            val view = WebView(this)
            view.settings.javaScriptEnabled = true
            view.settings.domStorageEnabled = true
            view.addJavascriptInterface(
                DevBridge(
                    onPicked = { },
                    onCycleEvent = { json -> handler.post { onCycle(json) } }
                ),
                "TermavigatorDev"
            )
            view.webViewClient = object : WebViewClient() {
                override fun onPageFinished(v: WebView?, url: String?) {
                    if (sent) return
                    sent = true
                    setStatus("enviando prompt")
                    v?.evaluateJavascript(DomScripts.sendScript(profile, prompt), null)
                }
            }
            // Dá tamanho ao WebView sem janela, para o site renderizar o layout
            view.layout(0, 0, 1080, 1920)
            webView = view
            setStatus("carregando site")
            view.loadUrl(profile.url)
        }
    }

    private fun onCycle(json: String) {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return
        when (o.optString("state")) {
            "sending" -> setStatus("aguardando resposta")
            "done" -> {
                BridgeStatus.setResult(o.optString("text"))
                finishTask("resposta recebida")
            }
            "timeout" -> finishTask("tempo esgotado")
            else -> finishTask("erro: " + o.optString("message", "desconhecido"))
        }
    }

    private fun setStatus(text: String) {
        BridgeStatus.setState(text)
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun finishTask(message: String) {
        BridgeStatus.setState(message)
        handler.post { releaseWebView() }
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

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Ponte com a IA", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
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
        const val ACTION_SEND = "com.example.devmode.SEND"
        const val ACTION_STOP = "com.example.devmode.STOP"
        const val EXTRA_PROFILE = "profile"
        const val EXTRA_PROMPT = "prompt"
        private const val CHANNEL_ID = "termai_bridge"
        private const val NOTIFICATION_ID = 4101

        fun send(context: Context, profile: SiteProfile, prompt: String) {
            val intent = Intent(context, TermaiBridgeService::class.java)
                .setAction(ACTION_SEND)
                .putExtra(EXTRA_PROFILE, profile.toJson().toString())
                .putExtra(EXTRA_PROMPT, prompt)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
