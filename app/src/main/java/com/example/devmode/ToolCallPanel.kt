package com.example.devmode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/** Mostra as chamadas lidas de uma resposta e permite devolver um resultado de teste ao chat. */
@Composable
fun ToolCallPanel(text: String, onSendResult: (String) -> Unit) {
    val parsed = remember(text) { ToolCallProtocol.parse(text) }
    var resultText by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Chamadas detectadas: " + parsed.calls.size + " · erros de formato: " + parsed.errors.size,
            style = MaterialTheme.typography.bodySmall
        )
        parsed.calls.forEach { call ->
            Text(
                "• " + call.tool + " (" + call.id + "): " + call.args.toString().take(200),
                style = MaterialTheme.typography.bodySmall
            )
        }
        parsed.errors.forEach { err ->
            Text(
                "Erro no bloco " + err.index + ": " + err.message,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = resultText,
            onValueChange = { resultText = it },
            label = { Text("Resultado para devolver (teste)") },
            modifier = Modifier.fillMaxWidth()
        )

        val firstCall = parsed.calls.firstOrNull()
        val blocks = ToolCallProtocol.buildResult(
            firstCall?.id ?: "teste",
            firstCall?.tool ?: "teste",
            resultText
        )
        Row {
            Button(
                enabled = blocks.size == 1 && resultText.isNotEmpty(),
                onClick = { onSendResult(blocks.first()) }
            ) { Text("Devolver ao chat") }
            TextButton(onClick = {
                clipboard.setText(
                    AnnotatedString(
                        ToolCallProtocol.protocolInstructions(listOf("ler_pagina", "buscar"))
                    )
                )
            }) { Text("Copiar protocolo") }
        }
        if (blocks.size > 1) {
            Text(
                "Resultado com " + blocks.size + " partes: envio em partes ainda não está disponível.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
