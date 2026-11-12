package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionName: String,
    val apkUrl: String,
    val apkSize: Long,
    val notes: String
)

sealed class UpdateCheck {
    object UpToDate : UpdateCheck()
    data class Available(val info: UpdateInfo) : UpdateCheck()
    data class Error(val message: String) : UpdateCheck()
}

/**
 * Verifica, baixa e instala a versão mais recente publicada nos releases do GitHub.
 * Não usa token: o repositório é público e a API de releases é anônima.
 */
object AppUpdater {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/Sjos0/Termavigator/releases/latest"
    private const val APK_NAME = "termavigator-update.apk"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 30_000

    fun checkForUpdate(): UpdateCheck {
        return try {
            val conn = open(LATEST_RELEASE_URL)
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val release = JSONObject(body)
            val remoteVersion = release.getString("tag_name").removePrefix("v")

            if (!isNewer(remoteVersion, BuildConfig.VERSION_NAME)) {
                return UpdateCheck.UpToDate
            }

            val assets = release.getJSONArray("assets")
            var apk: JSONObject? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
                    apk = asset
                    break
                }
            }

            if (apk == null) {
                UpdateCheck.Error("A versão $remoteVersion ainda não tem APK anexado.")
            } else {
                UpdateCheck.Available(
                    UpdateInfo(
                        versionName = remoteVersion,
                        apkUrl = apk.getString("browser_download_url"),
                        apkSize = apk.optLong("size", 0L),
                        notes = release.optString("body", "")
                    )
                )
            }
        } catch (e: Exception) {
            UpdateCheck.Error("Não foi possível verificar atualizações. Confira sua conexão.")
        }
    }

    /** Baixa o APK para o cache do app, reportando progresso de 0.0 a 1.0. */
    fun download(context: Context, info: UpdateInfo, onProgress: (Float) -> Unit): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val target = File(dir, APK_NAME)
        if (target.exists()) target.delete()

        val conn = open(info.apkUrl)
        val total = if (info.apkSize > 0) info.apkSize else conn.contentLengthLong
        conn.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(16 * 1024)
                var downloaded = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    if (total > 0) {
                        onProgress((downloaded.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        }
        conn.disconnect()

        if (total > 0 && target.length() != total) {
            target.delete()
            throw IOException("Download incompleto")
        }
        return target
    }

    /**
     * Abre o instalador do sistema.
     * Retorna false se o usuário ainda precisa liberar a instalação de apps desconhecidos
     * para o Termavigator; nesse caso abre a tela de permissão.
     */
    fun install(context: Context, apk: File): Boolean {
        if (!context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.packageName)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return false
        }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Termavigator-Updater")
        }

    internal fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split('.').map { it.toIntOrNull() ?: 0 }
        val l = local.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }
}
