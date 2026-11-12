package com.example.devmode

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject
import org.json.JSONTokener

enum class Role(val label: String) {
    INPUT("Campo de entrada"),
    SEND("Botão enviar"),
    THINKING("Pensando"),
    RESPONSE("Área da resposta"),
    STOP("Botão parar")
}

private const val BRIDGE_NAME = "TermavigatorDev"

/** Ponte exposta à página só enquanto a tela de modo desenvolvedor estiver aberta. */
class DevBridge(private val onPicked: (String) -> Unit) {
    @JavascriptInterface
    fun picked(json: String) {
        onPicked(json)
    }
}

/** evaluateJavascript devolve o valor como JSON; isto desfaz o encapsulamento de texto. */
private fun unquote(raw: String): String =
    runCatching { JSONTokener(raw).nextValue() }.getOrNull()?.toString() ?: raw

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DeveloperModeScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val store = remember { SiteProfileStore(context) }
    val handler = remember { Handler(Looper.getMainLooper()) }

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

    fun applyPick(json: String) {
        val selector = runCatching { JSONObject(json).optString("selector") }.getOrDefault("")
        if (selector.isBlank()) return
        selectors[currentRole] = selector
        message = currentRole.label + ": " + selector
        pickMode = false
        currentRole = Role.values()[(currentRole.ordinal + 1) % Role.values().size]
    }

    val bridge = remember { DevBridge { json -> handler.post { applyPick(json) } } }

    fun runJs(script: String, onResult: (String) -> Unit = {}) {
        webView?.evaluateJavascript(script) { raw -> onResult(unquote(raw ?: "")) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBackClick) { Text("Voltar") }
            Text("Modo desenvolvedor", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "Use apenas com sites que você controla. A página aberta aqui recebe uma ponte de JavaScript.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))

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

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                pickMode = true
                message = "Toque no elemento da página para: " + currentRole.label
                runJs(DomScripts.PICKER_INSTALL + "\n" + DomScripts.PICK_ON)
            }) { Text(if (pickMode) "Aguardando toque..." else "Selecionar") }

            Button(onClick = { runJs(DomScripts.EXTRACT) { extracted = it } }) {
                Text("Extrair")
            }

            Button(onClick = {
                val selector = selectors[currentRole]
                if (selector.isNullOrBlank()) {
                    message = "Selecione o elemento antes de testar."
                } else {
                    runJs(DomScripts.countScript(selector)) { count ->
                        message = currentRole.label + ": " + count + " elemento(s) encontrado(s)"
                    }
                }
            }) { Text("Testar") }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
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

        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Role.values().forEach { role ->
                Text(
                    role.label + ": " + (selectors[role] ?: "não definido"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

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

        message?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            factory = { ctx ->
                WebView(ctx).apply {
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
