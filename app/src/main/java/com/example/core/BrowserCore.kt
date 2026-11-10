package com.example.core

import com.example.core.models.BookmarkItem
import com.example.core.models.DownloadItem
import com.example.core.models.HistoryItem
import com.example.core.models.TabItem
import com.example.core.models.TabState
import kotlinx.coroutines.flow.StateFlow

interface BrowserCore {
    val tabsState: StateFlow<List<TabItem>>
    val currentTabState: StateFlow<TabItem?>
    val historyState: StateFlow<List<HistoryItem>>
    val bookmarksState: StateFlow<List<BookmarkItem>>
    val downloadsState: StateFlow<List<DownloadItem>>

    fun normalizeInput(input: String): String
    suspend fun createTab(url: String = "", isPrivate: Boolean = false): TabItem
    suspend fun selectTab(tabId: String)
    suspend fun closeTab(tabId: String)
    suspend fun archiveTab(tabId: String)
    suspend fun restoreArchivedTab(tabId: String)
    suspend fun pauseTab(tabId: String)
    suspend fun resumeTab(tabId: String)
    suspend fun updateCurrentTab(url: String, title: String)

    suspend fun addBookmark(url: String, title: String)
    suspend fun deleteBookmark(id: Long)

    suspend fun addHistory(url: String, title: String)
    suspend fun clearHistory()

    suspend fun addDownload(name: String, path: String, totalBytes: Long)
    suspend fun deleteDownload(id: Long)

    fun shouldBlockUrl(url: String): Boolean
    fun trimMemory(level: Int)
}
