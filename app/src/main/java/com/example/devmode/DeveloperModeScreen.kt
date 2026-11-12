package com.example.devmode

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL

enum class Role(val label: String) {
    INPUT("Campo de entrada"),
    SEND("Botão enviar"),
    THINKING("Pensando"),
    RESPONSE("Área da resposta"),
    STOP("Botão parar")
}

private const val BRIDGE_NAME = "TermavigatorDev"

/** Ponte exposta à página enquanto a tela de modo desenvolvedor estiver aberta. */
class DevBridge(
    private val onPicked: (String) -> Unit,
    private val onCycleEvent: (String) -> Unit
) {
    @JavascriptInterface
    fun picked(json: String) {
        onPicked(json)
    }

    @JavascriptInterface
    fun onCycle(json: String) {
        onCycleEvent(json)
    }
}

/** evaluateJavascript devolve o valor em JSON; isto desfaz o encapsulamento de texto. */
private fun unquote(raw: String): String =
    runCatching { JSONTokener(raw).nextValue() }.getOrNull()?.toString() ?: raw

/** Chama a API local da ponte, do mesmo jeito que o TermAI faria. Devolve status e corpo. */
private fun callLocalApi(token: String, prompt: String): String {
    val conn = URL("http://127.0.0.1:" + TermaiBridgeService.DEFAULT_PORT + "/v1/chat/completions")
        .openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.connectTimeout = 5000
    conn.readTimeout = 600_000
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("Authorization", "Bearer " + token)
    val body = JSONObject()
        .put("model", "ia-do-site")
        .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        .toString()
    conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    conn.disconnect()
    return "HTTP " + code + "\n" + text
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DeveloperModeScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val store = remember { SiteProfileStore(context) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val bgArgb = MaterialTheme.colorScheme.background.toArgb()

    var addressText by remember { mutableStateOf("https://") }
    var loadedUrl by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var pickMode by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf(Role.INPUT) }
    val selectors = remember { mutableStateMapOf<Role, String>() }
    var profileName by remember { mutableStateOf("") }
    var profiles by remember { mutableStateOf(store.load()) }
    var message by remember { mutableStateOf<String?>(null) }
    var extracted by remember { mutableStateOf<String?>(null) }
    var testPrompt by remember { mutableStateOf("Responda apenas: OK") }
    var cycleStatus by remember { mutableStateOf<String?>(null) }
    var cycleText by remember { mutableStateOf("") }
    var apiResult by remember { mutableStateOf("") }
    val bridgeToken = remember { TermaiBridgeService.bridgeToken(context) }
    val bridgeState by BridgeStatus.state.collectAsState()
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    fun currentTarget(): SiteProfile? {
        val target = SiteProfile(
            name = "ponte",
            url = loadedUrl.orEmpty(),
            input = selectors[Role.INPUT].orEmpty(),
            send = selectors[Role.SEND].orEmpty(),
            thinking = selectors[Role.THINKING].orEmpty(),
            response = selectors[Role.RESPONSE].orEmpty(),
            stop = selectors[Role.STOP].orEmpty()
        )
        val ok = target.url.isNotBlank() && target.input.isNotBlank() &&
            target.send.isNotBlank() && target.response.isNotBlank()
        return if (ok) target else null
    }

    fun applyPick(json: String) {
        val selector = runCatching { JSONObject(json).optString("selector") }.getOrDefault("")
        if (selector.isBlank()) return
        selectors[currentRole] = selector
        message = currentRole.label + ": " + selector
        pickMode = false
        currentRole = Role.values()[(currentRole.ordinal + 1) % Role.values().size]
    }

    fun handleCycle(json: String) {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return
        val state = o.optString("state")
        cycleStatus = when (state) {
            "sending" -> "enviando"
            "done" -> "resposta recebida"
            "timeout" -> "tempo esgotado"
            else -> o.optString("message", "erro")
        }
        if (state == "done" || state == "timeout") {
            cycleText = o.optString("text")
        }
    }

    val bridge = remember {
        DevBridge(
            onPicked = { json -> handler.post { applyPick(json) } },
            onCycleEvent = { json -> handler.post { handleCycle(json) } }
        )
    }

    fun runJs(script: String, onResult: (String) -> Unit = {}) {
        webView?.evaluateJavascript(script) { raw -> onResult(unquote(raw ?: "")) }
    }

    fun testApi() {
        scope.launch {
            apiResult = "verificando a ponte..."
            if (!BridgeStatus.state.value.startsWith("ponte ativa")) {
                val target = currentTarget()
                if (target == null) {
                    apiResult = "Defina o site e os seletores de entrada, enviar e resposta antes de testar a API."
                    return@launch
                }
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                TermaiBridgeService.startBridge(context, target)
                delay(2000)
            }
            apiResult = "chamando a API local..."
            val result = withContext(Dispatchers.IO) {
                runCatching { callLocalApi(bridgeToken, testPrompt) }
                    .getOrElse { "erro: " + (it.message ?: it.javaClass.simpleName) }
            }
            apiResult = result
        }
    }

    LaunchedEffect(loadedUrl) {
        loadedUrl?.let { webView?.loadUrl(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                removeJavascriptInterface(BRIDGE_NAME)
                stopLoading()
                destroy()
            }
            webView = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Painel de controle: rolável e separado do navegador da página
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBackClick) { Text("Voltar") }
                Text("Modo desenvolvedor", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "Use apenas com sites que você controla. A página abaixo recebe uma ponte de JavaScript.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    singleLine = true,
                    label = { Text("Endereço do site") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { loadedUrl = addressText.trim() }) { Text("Abrir") }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Role.values().forEach { role ->
                    FilterChip(
                        selected = currentRole == role,
                        onClick = { currentRole = role },
                        label = { Text(role.label) }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    pickMode = true
                    message = "Toque no elemento da página para: " + currentRole.label
                    runJs(DomScripts.PICKER_INSTALL + "\n" + DomScripts.PICK_ON)
                }) { Text(if (pickMode) "Aguardando toque..." else "Selecionar") }
                Button(onClick = { runJs(DomScripts.EXTRACT) { extracted = it } }) { Text("Extrair") }
                Button(onClick = {
                    val selector = selectors[currentRole]
                    if (selector.isNullOrBlank()) {
                        message = "Selecione o elemento antes de testar."
                    } else {
                        runJs(DomScripts.countScript(selector)) { count ->
                            message = currentRole.label + ": " + count + " elemento(s) encontrado(s)"
                        }
                    }
                }) { Text("Testar seletor") }
            }

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Role.values().forEach { role ->
                    Text(
                        role.label + ": " + (selectors[role] ?: "não definido"),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    singleLine = true,
                    label = { Text("Nome do perfil") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val name = profileName.trim()
                    val url = loadedUrl
                    if (name.isEmpty() || url == null) {
                        message = "Informe um nome e abra o site antes de salvar."
                    } else {
                        store.save(
                            SiteProfile(
                                name = name,
                                url = url,
                                input = selectors[Role.INPUT].orEmpty(),
                                send = selectors[Role.SEND].orEmpty(),
                                thinking = selectors[Role.THINKING].orEmpty(),
                                response = selectors[Role.RESPONSE].orEmpty(),
                                stop = selectors[Role.STOP].orEmpty()
                            )
                        )
                        profiles = store.load()
                        message = "Perfil salvo: " + name
                    }
                }) { Text("Salvar") }
            }
            profiles.forEach { profile ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        profile.name + " · " + profile.url,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        store.delete(profile.name)
                        profiles = store.load()
                    }) { Text("Apagar") }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Envio de teste (pela página acima)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = testPrompt,
                onValueChange = { testPrompt = it },
                singleLine = true,
                label = { Text("Prompt de teste") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                val target = currentTarget()
                if (target == null) {
                    message = "Defina o site, entrada, enviar e resposta antes do teste."
                } else {
                    cycleText = ""
                    cycleStatus = "enviando"
                    runJs(DomScripts.sendScript(target, testPrompt))
                }
            }) { Text("Enviar teste") }
            cycleStatus?.let { Text("Status: " + it, style = MaterialTheme.typography.bodySmall) }
            if (cycleText.isNotBlank()) {
                Text(cycleText.take(2000), style = MaterialTheme.typography.bodySmall)
                ToolCallPanel(
                    text = cycleText,
                    onSendResult = { block ->
                        val target = currentTarget()
                        if (target != null) {
                            cycleText = ""
                            cycleStatus = "enviando resultado"
                            runJs(DomScripts.sendScript(target, block))
                        }
                    }
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("Ponte para o TermAI", style = MaterialTheme.typography.titleSmall)
            Text("Estado: " + bridgeState, style = MaterialTheme.typography.bodySmall)
            Text(
                "Endereço da API: http://127.0.0.1:" + TermaiBridgeService.DEFAULT_PORT + "/v1",
                style = MaterialTheme.typography.bodySmall
            )
            Text("Token: " + bridgeToken, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(onClick = {
                    val target = currentTarget()
                    if (target == null) {
                        message = "Defina o site, entrada, enviar e resposta antes de ativar a ponte."
                    } else {
                        if (Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        TermaiBridgeService.startBridge(context, target)
                        message = "Ponte iniciada."
                    }
                }) { Text("Ativar ponte") }
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(bridgeToken))
                    message = "Token copiado."
                }) { Text("Copiar token") }
                TextButton(onClick = { TermaiBridgeService.stopBridge(context) }) { Text("Parar ponte") }
            }
            Spacer(Modifier.height(6.dp))
            Button(onClick = { testApi() }) { Text("Testar API local") }
            if (apiResult.isNotBlank()) {
                Text(apiResult.take(3000), style = MaterialTheme.typography.bodySmall)
            }

            message?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Navegador da página: fora de qualquer área rolável, com altura definida pelo peso
        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(0.45f),
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(bgArgb)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    addJavascriptInterface(bridge, BRIDGE_NAME)
                    webView = this
                }
            }
        )
    }

    extracted?.let { text ->
        AlertDialog(
            onDismissRequest = { extracted = null },
            title = { Text("Estrutura da página") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text.take(6000), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(text))
                    message = "Estrutura copiada."
                    extracted = null
                }) { Text("Copiar") }
            },
            dismissButton = {
                TextButton(onClick = { extracted = null }) { Text("Fechar") }
            }
        )
    }
}
