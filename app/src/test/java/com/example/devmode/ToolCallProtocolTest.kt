package com.example.devmode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToolCallProtocolTest {

    private val fence = "```"

    @Test
    fun parsesValidBlock() {
        val text = "Vou buscar.\n" + fence + "termai-tool\n" +
            "{\"id\": \"c1\", \"tool\": \"buscar\", \"args\": {\"q\": \"x\"}}\n" + fence + "\n"
        val r = ToolCallProtocol.parse(text)
        assertEquals(1, r.calls.size)
        assertEquals("buscar", r.calls[0].tool)
        assertEquals("c1", r.calls[0].id)
        assertEquals("x", r.calls[0].args.getString("q"))
        assertTrue(r.errors.isEmpty())
    }

    @Test
    fun reportsInvalidJsonWithoutDroppingIt() {
        val text = fence + "termai-tool\n{tool: buscar\n" + fence
        val r = ToolCallProtocol.parse(text)
        assertEquals(0, r.calls.size)
        assertEquals(1, r.errors.size)
        assertEquals(1, r.errors[0].index)
    }

    @Test
    fun reportsMissingTool() {
        val text = fence + "termai-tool\n{\"args\": {}}\n" + fence
        val r = ToolCallProtocol.parse(text)
        assertEquals(0, r.calls.size)
        assertEquals(1, r.errors.size)
    }

    @Test
    fun rejectsInvalidToolName() {
        val text = fence + "termai-tool\n{\"tool\": \"Buscar Web\"}\n" + fence
        val r = ToolCallProtocol.parse(text)
        assertEquals(0, r.calls.size)
        assertEquals(1, r.errors.size)
    }

    @Test
    fun generatesIdWhenMissing() {
        val text = fence + "termai-tool\n{\"tool\": \"buscar\"}\n" + fence
        val r = ToolCallProtocol.parse(text)
        assertEquals("call-1", r.calls[0].id)
    }

    @Test
    fun ignoresOtherFences() {
        val text = fence + "json\n{\"tool\": \"buscar\"}\n" + fence
        val r = ToolCallProtocol.parse(text)
        assertEquals(0, r.calls.size)
        assertEquals(0, r.errors.size)
    }

    @Test
    fun splitsLongResultsInParts() {
        val output = "a".repeat(5000)
        val parts = ToolCallProtocol.buildResult("c1", "buscar", output)
        assertEquals(3, parts.size)
        assertTrue(parts[0].contains("termai-result"))
        assertTrue(parts[2].contains("\"part\":3"))
    }
}
