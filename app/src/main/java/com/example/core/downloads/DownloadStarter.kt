package com.example.core.downloads

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import com.example.core.BrowserCore
import com.example.core.models.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Inicia downloads pelo DownloadManager do sistema e mantém o registro no app atualizado.
 * Os arquivos ficam na pasta de downloads do próprio app, sem precisar de permissão de armazenamento.
 */
class DownloadStarter(
    private val context: Context,
    private val core: BrowserCore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun start(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val target = uniqueFile(dir, fileName)

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(target.name)
            .setMimeType(mimeType)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, target.name)
        if (!userAgent.isNullOrBlank()) {
            request.addRequestHeader("User-Agent", userAgent)
        }
        val systemId = manager.enqueue(request)

        scope.launch {
            val recordId = core.addDownload(target.name, target.absolutePath, contentLength.coerceAtLeast(0L))
            track(systemId, recordId)
        }
    }

    private suspend fun track(systemId: Long, recordId: Long) {
        while (true) {
            var status = DownloadStatus.IN_PROGRESS
            var downloaded = 0L
            var total = 0L
            manager.query(DownloadManager.Query().setFilterById(systemId))?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val systemStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    status = when (systemStatus) {
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.IN_PROGRESS
                    }
                } else {
                    status = DownloadStatus.FAILED
                }
            }
            core.updateDownload(recordId, status, downloaded, total.coerceAtLeast(0L))
            if (status != DownloadStatus.IN_PROGRESS) break
            delay(1000)
        }
    }

    /** Evita sobrescrever um arquivo existente: acrescenta um número antes do nome. */
    private fun uniqueFile(dir: File, name: String): File {
        var candidate = File(dir, name)
        var index = 1
        while (candidate.exists()) {
            candidate = File(dir, index.toString() + "_" + name)
            index++
        }
        return candidate
    }
}
