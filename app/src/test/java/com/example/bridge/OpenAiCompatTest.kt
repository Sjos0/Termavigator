package com.example.bridge

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OpenAiCompatTest {

    private fun tools(): JSONArray = JSONArray(
        "[{\"type\":\"function\",\"function\":{\"name\":\"ler\",\"parameters\":" +
            "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}},\"required\":[\"path\"]}}}]"
    )

    @Test
    fun keepsTextBeforeToolCalls() {
        val r = OpenAiCompat.parseReply(
            "Vou ler o arquivo.\n<tool_call><name>ler</name><arg>a.txt</arg></tool_call>", tools(), "1"
        )
        assertEquals("Vou ler o arquivo.", r.text)
        assertEquals(1, r.toolCalls.size)
        assertEquals("ler", r.toolCalls[0].name)
        assertEquals("a.txt", JSONObject(r.toolCalls[0].argumentsJson).getString("path"))
    }

    @Test
    fun acceptsJsonArguments() {
        val r = OpenAiCompat.parseReply(
            "<tool_call><name>ler</name><arguments>{\"path\": \"b.txt\"}</arguments></tool_call>", tools(), "2"
        )
        assertEquals("b.txt", JSONObject(r.toolCalls[0].argumentsJson).getString("path"))
    }

    @Test
    fun numbersCallsInOrder() {
        val r = OpenAiCompat.parseReply(
            "<tool_call><name>ler</name><arg>x</arg></tool_call> e " +
                "<tool_call><name>ler</name><arg>y</arg></tool_call>", tools(), "3"
        )
        assertEquals(listOf("call_3_1", "call_3_2"), r.toolCalls.map { it.id })
        assertEquals("e", r.text)
    }

    @Test
    fun completionWithoutToolsIsStop() {
        val r = OpenAiCompat.parseReply("Oi", null, "4")
        val choice = OpenAiCompat.completion("m", r, 10).getJSONArray("choices").getJSONObject(0)
        assertEquals("stop", choice.getString("finish_reason"))
        assertEquals("Oi", choice.getJSONObject("message").getString("content"))
    }

    @Test
    fun completionWithToolsUsesStringArguments() {
        val r = OpenAiCompat.parseReply("<tool_call><name>ler</name><arg>c.txt</arg></tool_call>", tools(), "5")
        val choice = OpenAiCompat.completion("m", r, 10).getJSONArray("choices").getJSONObject(0)
        assertEquals("tool_calls", choice.getString("finish_reason"))
        val call = choice.getJSONObject("message").getJSONArray("tool_calls").getJSONObject(0)
        assertEquals("ler", call.getJSONObject("function").getString("name"))
        assertTrue(call.getJSONObject("function").get("arguments") is String)
    }

    @Test
    fun mirrorSendsOnlyTheDeltaWhenHistoryContinues() {
        val m = ConversationMirror()
        val first = listOf(
            IncomingTurn("system", "sistema", null, emptyList()),
            IncomingTurn("user", "oi", null, emptyList())
        )
        assertTrue(m.plan(first).newChat)
        m.commit(first, ParsedReply("Olá", listOf(ParsedToolCall("call_1_1", "ler", "{}"))))
        val next = first +
            IncomingTurn("assistant", "Olá", null, listOf("ler")) +
            IncomingTurn("tool", "conteudo", "call_1_1", emptyList())
        val p = m.plan(next)
        assertFalse(p.newChat)
        assertEquals(1, p.turns.size)
        assertTrue(m.render(p).contains("Resultado da ferramenta ler"))
    }

    @Test
    fun mirrorStartsNewChatWhenHistoryDiverges() {
        val m = ConversationMirror()
        val first = listOf(IncomingTurn("user", "oi", null, emptyList()))
        m.commit(first, ParsedReply("Olá", emptyList()))
        val other = listOf(IncomingTurn("user", "outra coisa", null, emptyList()))
        assertTrue(m.plan(other).newChat)
    }
}
