package com.example.bridge

/**
 * Espelho do que já foi enviado ao chat do site.
 * O TermAI manda o histórico inteiro a cada chamada; o site guarda a conversa dele.
 * Então só o que é novo vai ao site, e um chat novo começa quando o histórico diverge.
 */
class ConversationMirror {

    class Plan(val newChat: Boolean, val turns: List<IncomingTurn>)

    private val sent = mutableListOf<String>()
    private val toolNames = mutableMapOf<String, String>()

    fun plan(incoming: List<IncomingTurn>): Plan {
        val sigs = incoming.map { OpenAiCompat.signature(it) }
        val continues = sent.isNotEmpty() &&
            sent.size < sigs.size &&
            sent.indices.all { sent[it] == sigs[it] }
        return if (continues) {
            Plan(false, incoming.subList(sent.size, incoming.size))
        } else {
            Plan(true, incoming)
        }
    }

    /** Texto que vai ao site. Cada turno novo vira uma parte do texto, sem editar o que já foi enviado. */
    fun render(plan: Plan): String {
        return plan.turns.joinToString("\n\n") { t ->
            when (t.role) {
                "system" -> "Instruções do sistema:\n" + t.content
                "tool" -> "Resultado da ferramenta " +
                    (toolNames[t.toolCallId.orEmpty()] ?: t.toolCallId.orEmpty()) + ":\n" + t.content
                "assistant" -> "Assistente:\n" + t.content
                else -> t.content
            }
        }
    }

    /** Registra o histórico completo recebido e a resposta devolvida ao TermAI. */
    fun commit(incoming: List<IncomingTurn>, reply: ParsedReply) {
        reply.toolCalls.forEach { toolNames[it.id] = it.name }
        sent.clear()
        incoming.forEach { sent.add(OpenAiCompat.signature(it)) }
        sent.add(OpenAiCompat.signature(IncomingTurn("assistant", reply.text, null, reply.toolCalls.map { it.name })))
    }

    fun reset() {
        sent.clear()
        toolNames.clear()
    }
}
