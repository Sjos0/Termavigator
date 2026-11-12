package com.example.devmode

import org.json.JSONObject

/** Chamada de ferramenta lida do chat do site. */
data class ToolCall(val id: String, val tool: String, val args: JSONObject)

/** Bloco que não pôde ser lido. O índice começa em 1, para mostrar ao usuário. */
data class ToolCallError(val index: Int, val message: String, val raw: String)

data class ToolParseResult(val calls: List<ToolCall>, val errors: List<ToolCallError>)

/** Formato combinado com a IA do site: blocos de código marcados com termai-tool e termai-result. */
object ToolCallProtocol {

    const val FENCE = "termai-tool"
    const val RESULT_FENCE = "termai-result"
    const val MAX_RESULT_CHARS = 2000

    private val BLOCK = Regex("```" + FENCE + "\\s*\\n([\\s\\S]*?)```")
    private val TOOL_NAME = Regex("[a-z][a-z0-9_]{1,40}")

    /** Lê todos os blocos de chamada de uma resposta. Blocos inválidos viram erro, não são descartados. */
    fun parse(text: String): ToolParseResult {
        val calls = mutableListOf<ToolCall>()
        val errors = mutableListOf<ToolCallError>()
        BLOCK.findAll(text).forEachIndexed { i, match ->
            val raw = match.groupValues[1].trim()
            try {
                val o = JSONObject(raw)
                val tool = o.optString("tool").trim()
                if (tool.isEmpty()) throw IllegalArgumentException("campo tool ausente")
                if (!TOOL_NAME.matches(tool)) throw IllegalArgumentException("nome de ferramenta inválido: " + tool)
                val id = o.optString("id").trim().ifEmpty { "call-" + (i + 1) }
                calls.add(ToolCall(id, tool, o.optJSONObject("args") ?: JSONObject()))
            } catch (e: Exception) {
                errors.add(ToolCallError(i + 1, e.message ?: "JSON inválido", raw))
            }
        }
        return ToolParseResult(calls, errors)
    }

    /** Monta o resultado de uma ferramenta em uma ou mais partes, cada uma em um bloco termai-result. */
    fun buildResult(id: String, tool: String, output: String): List<String> {
        val chunks = output.chunked(MAX_RESULT_CHARS).ifEmpty { listOf("") }
        return chunks.mapIndexed { i, chunk ->
            val payload = JSONObject()
                .put("id", id)
                .put("tool", tool)
                .put("part", i + 1)
                .put("total", chunks.size)
                .put("data", chunk)
            "```" + RESULT_FENCE + "\n" + payload.toString() + "\n```"
        }
    }

    /** Instruções que o harness envia no começo da sessão, dentro do prompt. */
    fun protocolInstructions(tools: List<String>): String {
        return "Você pode usar ferramentas. Para chamar uma, responda com um bloco exatamente neste formato:\n" +
            "```" + FENCE + "\n" +
            "{\"id\": \"identificador-unico\", \"tool\": \"nome_da_ferramenta\", \"args\": {\"chave\": \"valor\"}}\n" +
            "```\n" +
            "Regras:\n" +
            "- Use somente estas ferramentas: " + tools.joinToString(", ") + ".\n" +
            "- Um bloco por chamada, com apenas JSON válido dentro.\n" +
            "- Depois do bloco, pare e espere. O resultado chega em um bloco " + "```" + RESULT_FENCE +
            " com os campos id, part e total.\n" +
            "- Se o resultado vier em partes, espere todas antes de continuar."
    }
}
