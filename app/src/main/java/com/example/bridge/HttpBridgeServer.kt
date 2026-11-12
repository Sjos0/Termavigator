package com.example.bridge

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Servidor HTTP mínimo, só em 127.0.0.1, com autenticação por token (Bearer).
 * Sem dependências externas.
 */
class HttpBridgeServer(
    private val port: Int,
    private val token: String,
    private val handler: (method: String, path: String, body: String) -> Pair<Int, String>
) {
    @Volatile
    private var serverSocket: ServerSocket? = null

    fun start() {
        val socket = ServerSocket(port, 8, InetAddress.getByName("127.0.0.1"))
        serverSocket = socket
        val acceptor = Thread {
            while (!socket.isClosed) {
                val client = try {
                    socket.accept()
                } catch (e: Exception) {
                    break
                }
                Thread { handle(client) }.apply { isDaemon = true }.start()
            }
        }
        acceptor.isDaemon = true
        acceptor.name = "termai-bridge-accept"
        acceptor.start()
    }

    fun stop() {
        serverSocket?.close()
        serverSocket = null
    }

    private fun handle(client: Socket) {
        client.use { socket ->
            socket.soTimeout = 300_000
            val input = socket.getInputStream()
            val requestLine = readLine(input) ?: return
            val parts = requestLine.split(" ")
            val method = parts.getOrElse(0) { "" }
            val path = parts.getOrElse(1) { "/" }
            val headers = HashMap<String, String>()
            while (true) {
                val line = readLine(input) ?: break
                if (line.isEmpty()) break
                val idx = line.indexOf(':')
                if (idx > 0) {
                    headers[line.substring(0, idx).trim().lowercase()] = line.substring(idx + 1).trim()
                }
            }
            val length = headers["content-length"]?.toIntOrNull() ?: 0
            val bodyBytes = ByteArray(length.coerceAtLeast(0))
            var read = 0
            while (read < length) {
                val n = input.read(bodyBytes, read, length - read)
                if (n < 0) break
                read += n
            }
            val body = String(bodyBytes, 0, read, Charsets.UTF_8)
            val authorized = headers["authorization"] == "Bearer $token"
            val result = if (!authorized) {
                401 to "{\"error\":\"token inválido\"}"
            } else {
                handler(method, path, body)
            }
            val payload = result.second.toByteArray(Charsets.UTF_8)
            val head = "HTTP/1.1 " + result.first + " " + reason(result.first) + "\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Content-Length: " + payload.size + "\r\n" +
                "Connection: close\r\n\r\n"
            val out = socket.getOutputStream()
            out.write(head.toByteArray(Charsets.UTF_8))
            out.write(payload)
            out.flush()
        }
    }

    private fun readLine(input: InputStream): String? {
        val buffer = ByteArrayOutputStream()
        while (true) {
            val b = input.read()
            if (b < 0) {
                return if (buffer.size() == 0) null else buffer.toString(Charsets.UTF_8.name())
            }
            if (b == '\n'.code) break
            if (b != '\r'.code) buffer.write(b)
        }
        return buffer.toString(Charsets.UTF_8.name())
    }

    private fun reason(status: Int): String {
        return when (status) {
            200 -> "OK"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            404 -> "Not Found"
            504 -> "Gateway Timeout"
            else -> "Internal Server Error"
        }
    }
}
