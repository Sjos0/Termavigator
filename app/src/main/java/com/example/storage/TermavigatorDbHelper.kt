package com.example.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.core.models.BookmarkItem
import com.example.core.models.DownloadItem
import com.example.core.models.DownloadStatus
import com.example.core.models.HistoryItem
import com.example.core.models.TabItem
import com.example.core.models.TabState

class TermavigatorDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE tabs (
                id TEXT PRIMARY KEY,
                url TEXT NOT NULL,
                title TEXT NOT NULL,
                scroll_y INTEGER DEFAULT 0,
                state TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                last_visited_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                url TEXT NOT NULL,
                title TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE bookmarks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                url TEXT NOT NULL,
                title TEXT NOT NULL,
                folder TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE downloads (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                path TEXT NOT NULL,
                total_bytes INTEGER NOT NULL,
                downloaded_bytes INTEGER NOT NULL,
                status TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Seed initial default favorites matching reference images
        seedDefaultData(db)
    }

    private fun seedDefaultData(db: SQLiteDatabase) {
        val defaultBookmarks = listOf(
            BookmarkItem(url = "https://designinspiration.com", title = "Design Ideas"),
            BookmarkItem(url = "https://dailyreads.org", title = "Daily Reads"),
            BookmarkItem(url = "https://shopwise.com", title = "Favorite Shop"),
            BookmarkItem(url = "https://globalforum.net", title = "Global Forum"),
            BookmarkItem(url = "https://musicbeats.com", title = "Music Beats")
        )
        for (b in defaultBookmarks) {
            val cv = ContentValues().apply {
                put("url", b.url)
                put("title", b.title)
                put("folder", b.folder)
                put("created_at", b.createdAt)
            }
            db.insert("bookmarks", null, cv)
        }

        val defaultHistory = listOf(
            HistoryItem(url = "https://design.com", title = "Daily Design", timestamp = System.currentTimeMillis() - 1000 * 60 * 15),
            HistoryItem(url = "https://newsfeed.org", title = "World News", timestamp = System.currentTimeMillis() - 1000 * 60 * 60),
            HistoryItem(url = "https://termavigator.app/minimal-interfaces", title = "The Evolution of Minimal Digital Interfaces", timestamp = System.currentTimeMillis() - 1000 * 60 * 105),
            HistoryItem(url = "https://ui-trend.net", title = "Minimalist UI", timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 16),
            HistoryItem(url = "https://recipehub.com", title = "Recipe Hub", timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 19),
            HistoryItem(url = "https://creativestudio.io", title = "Creative Studio", timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 23)
        )
        for (h in defaultHistory) {
            val cv = ContentValues().apply {
                put("url", h.url)
                put("title", h.title)
                put("timestamp", h.timestamp)
            }
            db.insert("history", null, cv)
        }

        // Seed initial sample downloads as seen in T08 reference
        val defaultDownloads = listOf(
            DownloadItem(name = "User_Research_Assets.zip", path = "/downloads/User_Research_Assets.zip", totalBytes = 45200000, downloadedBytes = 18400000, status = DownloadStatus.IN_PROGRESS),
            DownloadItem(name = "Briefing_Document.pdf", path = "/downloads/Briefing_Document.pdf", totalBytes = 4800000, downloadedBytes = 4800000, status = DownloadStatus.COMPLETED),
            DownloadItem(name = "Creative_Portfolio.pdf", path = "/downloads/Creative_Portfolio.pdf", totalBytes = 22100000, downloadedBytes = 0, status = DownloadStatus.FAILED)
        )
        for (d in defaultDownloads) {
            val cv = ContentValues().apply {
                put("name", d.name)
                put("path", d.path)
                put("total_bytes", d.totalBytes)
                put("downloaded_bytes", d.downloadedBytes)
                put("status", d.status.name)
                put("timestamp", d.timestamp)
            }
            db.insert("downloads", null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tabs")
        db.execSQL("DROP TABLE IF EXISTS history")
        db.execSQL("DROP TABLE IF EXISTS bookmarks")
        db.execSQL("DROP TABLE IF EXISTS downloads")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "termavigator.db"
        const val DATABASE_VERSION = 1
    }
}
