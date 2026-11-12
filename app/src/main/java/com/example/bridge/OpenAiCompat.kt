package com.example.bridge

import org.json.JSONArray
import org.json.JSONObject

/** Mensagem recebida no formato de chat completions. */
data class IncomingTurn(
    val role: String,
    val content: String,
    val toolCallId: String?,
    val toolCalls: List<String>
)

/** Chamada de ferramenta lida do texto do site, já no formato da API. */
data class ParsedToolCall(val id: String, val name: String, val argumentsJson: String)

/** Resposta do site separada em texto (na ordem em que aparece) e chamadas de ferramenta. */
data class ParsedReply(val text: String, val toolCalls: List<ParsedToolCall>)

/** Conversão entre o formato de chat completions e o texto do site. */
object OpenAiCompat {

    private val TOOL_BLOCK = Regex(
        "<tool_call>(.*?)</tool_call>|<tool>(.*?)</tool>",
        RegexOption.DOT_MATCHES_ALL
    )

    fun signature(t: IncomingTurn): String =
        t.role + "|" + t.content + "|" + t.toolCalls.joinToString(",") + "|" + (t.toolCallId ?: "")

    fun parseMessages(body: JSONObject): List<IncomingTurn> {
        val arr = body.optJSONArray("messages") ?: return emptyList()
        return (0 until arr.length()).map { i ->
            val m = arr.getJSONObject(i)
            val calls = m.optJSONArray("tool_calls")
            val names = if (calls == null) {
                emptyList()
            } else {
                (0 until calls.length()).map { k ->
                    calls.getJSONObject(k).optJSONObject("function")?.optString("name").orEmpty()
                }
            }
            IncomingTurn(
                role = m.optString("role"),
                content = contentOf(m),
                toolCallId = m.optString("tool_call_id").takeIf { it.isNotEmpty() },
                toolCalls = names
            )
        }
    }

    private fun contentOf(m: JSONObject): String {
        return when (val c = m.opt("content")) {
            is String -> c
            is JSONArray -> (0 until c.length()).joinToString("\n") { c.optJSONObject(it)?.optString("text").orEmpty() }
            else -> ""
        }
    }

    /** Separa as chamadas de ferramenta do texto. O texto restante mantém a ordem original. */
    fun parseReply(raw: String, tools: JSONArray?, callPrefix: String): ParsedReply {
        val calls = mutableListOf<ParsedToolCall>()
        val text = TOOL_BLOCK.replace(raw) { match ->
            val inner = match.groups[1]?.value ?: match.groups[2]?.value ?: ""
            val name = tagValue(inner, "name", first = true).orEmpty()
            val arg = tagValue(inner, "arg", first = false)
                ?: tagValue(inner, "arguments", first = false)
                ?: ""
            if (name.isNotBlank()) {
                val id = "call_" + callPrefix + "_" + (calls.size + 1)
                calls.add(ParsedToolCall(id, name, buildArguments(name, arg, tools)))
            }
            ""
        }
        return ParsedReply(text.trim(), calls)
    }

    private fun tagValue(s: String, tag: String, first: Boolean): String? {
        val regex = Regex("<" + tag + ">(.*?)</" + tag + ">", RegexOption.DOT_MATCHES_ALL)
        val m = if (first) regex.find(s) else regex.findAll(s).lastOrNull()
        return m?.groupValues?.get(1)?.trim()
    }

    /** Converte o argumento do texto no JSON que a ferramenta espera, usando o schema recebido. */
    fun buildArguments(name: String, arg: String, tools: JSONArray?): String {
        val trimmed = arg.trim()
        if (trimmed.startsWith("{")) {
            runCatching { JSONObject(trimmed).toString() }.getOrNull()?.let { return it }
        }
        val params = parametersOf(name, tools)
        val key = params?.optJSONArray("required")?.optString(0)?.takeIf { it.isNotBlank() }
            ?: params?.optJSONObject("properties")?.keys()?.asSequence()?.firstOrNull()
            ?: "input"
        return JSONObject().put(key, trimmed).toString()
    }

    private fun parametersOf(name: String, tools: JSONArray?): JSONObject? {
        if (tools == null) return null
        for (i in 0 until tools.length()) {
            val fn = tools.optJSONObject(i)?.optJSONObject("function") ?: continue
            if (fn.optString("name") == name) return fn.optJSONObject("parameters")
        }
        return null
    }

    fun completion(model: String, reply: ParsedReply, promptChars: Int): JSONObject {
        val message = JSONObject().put("role", "assistant").put("content", reply.text)
        if (reply.toolCalls.isNotEmpty()) {
            val calls = JSONArray()
            reply.toolCalls.forEach { c ->
                calls.put(
                    JSONObject()
                        .put("id", c.id)
                        .put("type", "function")
                        .put("function", JSONObject().put("name", c.name).put("arguments", c.argumentsJson))
                )
            }
            message.put("tool_calls", calls)
        }
        val outChars = reply.text.length + reply.toolCalls.sumOf { it.argumentsJson.length }
        val inTokens = estimateTokens(promptChars)
        val outTokens = estimateTokens(outChars)
        return JSONObject()
            .put("id", "chatcmpl-" + System.currentTimeMillis())
            .put("object", "chat.completion")
            .put("created", System.currentTimeMillis() / 1000)
            .put("model", model)
            .put(
                "choices",
                JSONArray().put(
                    JSONObject()
                        .put("index", 0)
                        .put("message", message)
                        .put("finish_reason", if (reply.toolCalls.isNotEmpty()) "tool_calls" else "stop")
                )
            )
            .put(
                "usage",
                JSONObject()
                    .put("prompt_tokens", inTokens)
                    .put("completion_tokens", outTokens)
                    .put("total_tokens", inTokens + outTokens)
            )
    }

    /** Estimativa simples: cerca de 4 caracteres por token. O site não informa o uso real. */
    fun estimateTokens(chars: Int): Int = (chars / 4).coerceAtLeast(1)
}
