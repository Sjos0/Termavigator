package com.example

import com.example.blocking.ContentBlocker
import com.example.core.models.DownloadItem
import com.example.core.models.DownloadStatus
import com.example.core.models.TabItem
import com.example.core.models.TabState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ExampleUnitTest {

    @Test
    fun testContentBlocker() {
        val blocker = ContentBlocker(enabled = true)
        assertTrue(blocker.shouldBlock("https://adservice.google.com/ads?id=123"))
        assertTrue(blocker.shouldBlock("https://pagead2.googlesyndication.com/pagead/show_ads.js"))
        assertTrue(blocker.shouldBlock("https://secure.doubleclick.net/ad"))
        assertTrue(blocker.shouldBlock("https://track.criteo.com/event"))
        assertFalse(blocker.shouldBlock("https://termavigator.app/article"))
        assertFalse(blocker.shouldBlock("https://wikipedia.org"))
        assertFalse(blocker.shouldBlock("https://github.com"))

        // When blocker disabled
        blocker.setEnabled(false)
        assertFalse(blocker.shouldBlock("https://secure.doubleclick.net/ad"))
    }

    @Test
    fun testUrlNormalizationLogic() {
        fun normalize(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""
            val hasScheme = trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) ||
                    trimmed.startsWith("file://", ignoreCase = true) ||
                    trimmed.startsWith("about:", ignoreCase = true)
            if (hasScheme) return trimmed
            val hasDot = trimmed.contains(".") && !trimmed.contains(" ")
            if (hasDot) return "https://$trimmed"
            val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.toString())
            return "https://duckduckgo.com/?q=$encoded"
        }

        assertEquals("https://termavigator.app", normalize("termavigator.app"))
        assertEquals("https://google.com/search", normalize("https://google.com/search"))
        assertEquals("http://example.org", normalize("http://example.org"))
        assertTrue(normalize("brasil noticias").startsWith("https://duckduckgo.com/?q="))
    }

    @Test
    fun testTabAndDownloadModels() {
        val tab = TabItem(
            id = "test_1",
            url = "https://termavigator.app",
            title = "Termavigator",
            state = TabState.ACTIVE,
            isPrivate = false
        )
        assertEquals("test_1", tab.id)
        assertEquals(TabState.ACTIVE, tab.state)
        assertFalse(tab.isPrivate)

        val download = DownloadItem(
            id = 10,
            name = "Document.pdf",
            path = "/downloads/Document.pdf",
            totalBytes = 1024,
            downloadedBytes = 1024,
            status = DownloadStatus.COMPLETED
        )
        assertEquals(DownloadStatus.COMPLETED, download.status)
        assertEquals(1024L, download.totalBytes)
    }
}
