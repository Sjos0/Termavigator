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
