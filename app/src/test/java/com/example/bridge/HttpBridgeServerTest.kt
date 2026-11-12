package com.example.bridge

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL

@RunWith(RobolectricTestRunner::class)
class HttpBridgeServerTest {

    private fun freePort(): Int = ServerSocket(0).use { it.localPort }

    private fun get(port: Int, path: String, auth: String?): Int {
        val c = URL("http://127.0.0.1:" + port + path).openConnection() as HttpURLConnection
        c.requestMethod = "GET"
        if (auth != null) c.setRequestProperty("Authorization", auth)
        return c.responseCode
    }

    @Test
    fun healthNeedsTheRightToken() {
        val port = freePort()
        val server = HttpBridgeServer(port, "segredo") { _, _, _ -> 200 to "{\"ok\":true}" }
        server.start()
        try {
            assertEquals(200, get(port, "/health", "Bearer segredo"))
            assertEquals(401, get(port, "/health", "Bearer errado"))
            assertEquals(401, get(port, "/health", null))
        } finally {
            server.stop()
        }
    }

    @Test
    fun postBodyReachesTheHandler() {
        val port = freePort()
        var received = ""
        val server = HttpBridgeServer(port, "segredo") { _, _, body ->
            received = body
            200 to "{\"ok\":true}"
        }
        server.start()
        try {
            val c = URL("http://127.0.0.1:" + port + "/v1/chat/completions").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.doOutput = true
            c.setRequestProperty("Authorization", "Bearer segredo")
            c.setRequestProperty("Content-Type", "application/json")
            val payload = "{\"messages\":[{\"role\":\"user\",\"content\":\"olá ção\"}]}"
            c.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            assertEquals(200, c.responseCode)
            assertEquals(payload, received)
        } finally {
            server.stop()
        }
    }
}
