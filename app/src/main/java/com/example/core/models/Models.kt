package com.example.core.models

enum class TabState {
    ACTIVE,
    PAUSED,
    ARCHIVED
}

data class TabItem(
    val id: String,
    val url: String,
    val title: String,
    val scrollY: Int = 0,
    val state: TabState = TabState.ACTIVE,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastVisitedAt: Long = System.currentTimeMillis()
)

data class HistoryItem(
    val id: Long = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BookmarkItem(
    val id: Long = 0,
    val url: String,
    val title: String,
    val folder: String = "Geral",
    val createdAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class DownloadItem(
    val id: Long = 0,
    val name: String,
    val path: String,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val timestamp: Long = System.currentTimeMillis()
)
