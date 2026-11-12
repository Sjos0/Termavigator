package com.example.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.core.models.BookmarkItem
import com.example.core.models.DownloadItem
import com.example.core.models.DownloadStatus
import com.example.core.models.HistoryItem
import com.example.core.models.TabItem
import com.example.core.models.TabState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrowserRepository(context: Context) {
    private val dbHelper = TermavigatorDbHelper(context)

    // --- TABS PERSISTENCE ---
    suspend fun getAllTabs(): List<TabItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TabItem>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "tabs",
            null,
            null,
            null,
            null,
            null,
            "last_visited_at DESC"
        )
        cursor.use {
            val idCol = it.getColumnIndexOrThrow("id")
            val urlCol = it.getColumnIndexOrThrow("url")
            val titleCol = it.getColumnIndexOrThrow("title")
            val scrollYCol = it.getColumnIndexOrThrow("scroll_y")
            val stateCol = it.getColumnIndexOrThrow("state")
            val createdCol = it.getColumnIndexOrThrow("created_at")
            val visitedCol = it.getColumnIndexOrThrow("last_visited_at")

            while (it.moveToNext()) {
                list.add(
                    TabItem(
                        id = it.getString(idCol),
                        url = it.getString(urlCol),
                        title = it.getString(titleCol),
                        scrollY = it.getInt(scrollYCol),
                        state = runCatching { TabState.valueOf(it.getString(stateCol)) }.getOrDefault(TabState.ACTIVE),
                        isPrivate = false,
                        createdAt = it.getLong(createdCol),
                        lastVisitedAt = it.getLong(visitedCol)
                    )
                )
            }
        }
        list
    }

    suspend fun saveTab(tab: TabItem) = withContext(Dispatchers.IO) {
        if (tab.isPrivate) return@withContext // Do not persist private tabs!
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("id", tab.id)
            put("url", tab.url)
            put("title", tab.title)
            put("scroll_y", tab.scrollY)
            put("state", tab.state.name)
            put("created_at", tab.createdAt)
            put("last_visited_at", tab.lastVisitedAt)
        }
        db.insertWithOnConflict("tabs", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun deleteTab(tabId: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("tabs", "id = ?", arrayOf(tabId))
    }

    // --- HISTORY ---
    suspend fun getHistory(): List<HistoryItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<HistoryItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("history", null, null, null, null, null, "timestamp DESC")
        cursor.use {
            val idCol = it.getColumnIndexOrThrow("id")
            val urlCol = it.getColumnIndexOrThrow("url")
            val titleCol = it.getColumnIndexOrThrow("title")
            val timeCol = it.getColumnIndexOrThrow("timestamp")
            while (it.moveToNext()) {
                list.add(
                    HistoryItem(
                        id = it.getLong(idCol),
                        url = it.getString(urlCol),
                        title = it.getString(titleCol),
                        timestamp = it.getLong(timeCol)
                    )
                )
            }
        }
        list
    }

    suspend fun addHistory(url: String, title: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("url", url)
            put("title", title)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("history", null, cv)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("history", null, null)
    }

    // --- BOOKMARKS ---
    suspend fun getBookmarks(): List<BookmarkItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<BookmarkItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("bookmarks", null, null, null, null, null, "created_at DESC")
        cursor.use {
            val idCol = it.getColumnIndexOrThrow("id")
            val urlCol = it.getColumnIndexOrThrow("url")
            val titleCol = it.getColumnIndexOrThrow("title")
            val folderCol = it.getColumnIndexOrThrow("folder")
            val timeCol = it.getColumnIndexOrThrow("created_at")
            while (it.moveToNext()) {
                list.add(
                    BookmarkItem(
                        id = it.getLong(idCol),
                        url = it.getString(urlCol),
                        title = it.getString(titleCol),
                        folder = it.getString(folderCol),
                        createdAt = it.getLong(timeCol)
                    )
                )
            }
        }
        list
    }

    suspend fun addBookmark(url: String, title: String, folder: String = "Geral"): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("url", url)
            put("title", title)
            put("folder", folder)
            put("created_at", System.currentTimeMillis())
        }
        db.insert("bookmarks", null, cv)
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("bookmarks", "id = ?", arrayOf(id.toString()))
    }

    // --- DOWNLOADS ---
    suspend fun getDownloads(): List<DownloadItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DownloadItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("downloads", null, null, null, null, null, "timestamp DESC")
        cursor.use {
            val idCol = it.getColumnIndexOrThrow("id")
            val nameCol = it.getColumnIndexOrThrow("name")
            val pathCol = it.getColumnIndexOrThrow("path")
            val totalCol = it.getColumnIndexOrThrow("total_bytes")
            val downCol = it.getColumnIndexOrThrow("downloaded_bytes")
            val statCol = it.getColumnIndexOrThrow("status")
            val timeCol = it.getColumnIndexOrThrow("timestamp")
            while (it.moveToNext()) {
                list.add(
                    DownloadItem(
                        id = it.getLong(idCol),
                        name = it.getString(nameCol),
                        path = it.getString(pathCol),
                        totalBytes = it.getLong(totalCol),
                        downloadedBytes = it.getLong(downCol),
                        status = runCatching { DownloadStatus.valueOf(it.getString(statCol)) }.getOrDefault(DownloadStatus.COMPLETED),
                        timestamp = it.getLong(timeCol)
                    )
                )
            }
        }
        list
    }

    suspend fun addDownload(name: String, path: String, totalBytes: Long): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", name)
            put("path", path)
            put("total_bytes", totalBytes)
            put("downloaded_bytes", 0)
            put("status", DownloadStatus.IN_PROGRESS.name)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert("downloads", null, cv)
    }

    suspend fun updateDownload(id: Long, status: DownloadStatus, downloadedBytes: Long, totalBytes: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("status", status.name)
            put("downloaded_bytes", downloadedBytes)
            put("total_bytes", totalBytes)
        }
        db.update("downloads", cv, "id = ?", arrayOf(id.toString()))
    }

    suspend fun deleteDownload(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("downloads", "id = ?", arrayOf(id.toString()))
    }
}
