package com.example.core

import android.content.ComponentCallbacks2
import com.example.blocking.ContentBlocker
import com.example.core.models.BookmarkItem
import com.example.core.models.DownloadItem
import com.example.core.models.HistoryItem
import com.example.core.models.TabItem
import com.example.core.models.TabState
import com.example.storage.BrowserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class KotlinBrowserCore(
    private val repository: BrowserRepository,
    private val contentBlocker: ContentBlocker,
    private val coroutineScope: CoroutineScope
) : BrowserCore {

    private val _tabs = MutableStateFlow<List<TabItem>>(emptyList())
    override val tabsState: StateFlow<List<TabItem>> = _tabs.asStateFlow()

    private val _currentTab = MutableStateFlow<TabItem?>(null)
    override val currentTabState: StateFlow<TabItem?> = _currentTab.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    override val historyState: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<BookmarkItem>>(emptyList())
    override val bookmarksState: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    override val downloadsState: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    var maxActiveTabsLimit: Int = 3

    init {
        // Load initial data from SQLite
        coroutineScope.launch {
            loadInitialSession()
            refreshHistory()
            refreshBookmarks()
            refreshDownloads()
        }
    }

    private suspend fun loadInitialSession() {
        val storedTabs = repository.getAllTabs()
        if (storedTabs.isNotEmpty()) {
            _tabs.value = storedTabs
            _currentTab.value = storedTabs.firstOrNull { it.state == TabState.ACTIVE } ?: storedTabs.first()
        } else {
            // Create initial default open tabs as seen in reference screenshots
            val initialTabs = listOf(
                TabItem(
                    id = "tab_1",
                    url = "https://termavigator.app/minimal-interfaces",
                    title = "The Evolution of Minimal Digital Interfaces",
                    state = TabState.ACTIVE
                ),
                TabItem(
                    id = "tab_2",
                    url = "https://design.com",
                    title = "Daily Design",
                    state = TabState.ACTIVE
                ),
                TabItem(
                    id = "tab_3",
                    url = "https://newsfeed.org",
                    title = "World News",
                    state = TabState.ACTIVE
                ),
                TabItem(
                    id = "tab_4",
                    url = "https://ui-trend.net",
                    title = "Minimalist UI",
                    state = TabState.PAUSED
                ),
                TabItem(
                    id = "tab_5",
                    url = "https://shop.com",
                    title = "Shopping Cart",
                    state = TabState.PAUSED
                )
            )
            for (tab in initialTabs) {
                repository.saveTab(tab)
            }
            _tabs.value = initialTabs
            _currentTab.value = initialTabs.first()
        }
    }

    override fun normalizeInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val hasScheme = trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("file://", ignoreCase = true) ||
                trimmed.startsWith("about:", ignoreCase = true)

        if (hasScheme) return trimmed

        val hasDot = trimmed.contains(".") && !trimmed.contains(" ")
        if (hasDot) {
            return "https://$trimmed"
        }

        // Web search fallback
        val encoded = URLEncoder.encode(trimmed, StandardCharsets.UTF_8.toString())
        return "https://duckduckgo.com/?q=$encoded"
    }

    override suspend fun createTab(url: String, isPrivate: Boolean): TabItem {
        val normalizedUrl = if (url.isNotEmpty()) normalizeInput(url) else ""
        val newTab = TabItem(
            id = UUID.randomUUID().toString(),
            url = normalizedUrl,
            title = if (normalizedUrl.isNotEmpty()) normalizedUrl else "Nova aba",
            state = TabState.ACTIVE,
            isPrivate = isPrivate,
            createdAt = System.currentTimeMillis(),
            lastVisitedAt = System.currentTimeMillis()
        )

        val updated = listOf(newTab) + _tabs.value
        _tabs.value = updated
        _currentTab.value = newTab

        if (!isPrivate) {
            repository.saveTab(newTab)
        }

        enforceActiveTabsLimit()
        return newTab
    }

    override suspend fun selectTab(tabId: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        if (tab.state == TabState.PAUSED) {
            resumeTab(tabId)
        } else {
            _currentTab.value = tab
            val updated = _tabs.value.map {
                if (it.id == tabId) it.copy(lastVisitedAt = System.currentTimeMillis()) else it
            }
            _tabs.value = updated
            if (!tab.isPrivate) {
                repository.saveTab(tab.copy(lastVisitedAt = System.currentTimeMillis()))
            }
        }
    }

    override suspend fun closeTab(tabId: String) {
        val tabToClose = _tabs.value.find { it.id == tabId }
        val updated = _tabs.value.filter { it.id != tabId }
        _tabs.value = updated

        if (tabToClose != null && !tabToClose.isPrivate) {
            repository.deleteTab(tabId)
        }

        if (_currentTab.value?.id == tabId) {
            _currentTab.value = updated.firstOrNull { it.state != TabState.ARCHIVED }
        }
    }

    override suspend fun archiveTab(tabId: String) {
        val updated = _tabs.value.map {
            if (it.id == tabId) it.copy(state = TabState.ARCHIVED) else it
        }
        _tabs.value = updated
        val archived = updated.find { it.id == tabId }
        if (archived != null && !archived.isPrivate) {
            repository.saveTab(archived)
        }
        if (_currentTab.value?.id == tabId) {
            _currentTab.value = updated.firstOrNull { it.state != TabState.ARCHIVED }
        }
    }

    override suspend fun restoreArchivedTab(tabId: String) {
        val updated = _tabs.value.map {
            if (it.id == tabId) it.copy(state = TabState.ACTIVE, lastVisitedAt = System.currentTimeMillis()) else it
        }
        _tabs.value = updated
        val restored = updated.find { it.id == tabId }
        if (restored != null) {
            _currentTab.value = restored
            if (!restored.isPrivate) {
                repository.saveTab(restored)
            }
        }
        enforceActiveTabsLimit()
    }

    override suspend fun pauseTab(tabId: String) {
        val updated = _tabs.value.map {
            if (it.id == tabId) it.copy(state = TabState.PAUSED) else it
        }
        _tabs.value = updated
        val paused = updated.find { it.id == tabId }
        if (paused != null && !paused.isPrivate) {
            repository.saveTab(paused)
        }
    }

    override suspend fun resumeTab(tabId: String) {
        val updated = _tabs.value.map {
            if (it.id == tabId) it.copy(state = TabState.ACTIVE, lastVisitedAt = System.currentTimeMillis()) else it
        }
        _tabs.value = updated
        val resumed = updated.find { it.id == tabId }
        if (resumed != null) {
            _currentTab.value = resumed
            if (!resumed.isPrivate) {
                repository.saveTab(resumed)
            }
        }
        enforceActiveTabsLimit()
    }

    override suspend fun updateCurrentTab(url: String, title: String) {
        val current = _currentTab.value ?: return
        val updatedTab = current.copy(
            url = url,
            title = title.ifEmpty { url },
            lastVisitedAt = System.currentTimeMillis()
        )
        _currentTab.value = updatedTab
        _tabs.value = _tabs.value.map { if (it.id == updatedTab.id) updatedTab else it }

        if (!current.isPrivate) {
            repository.saveTab(updatedTab)
            repository.addHistory(url, title.ifEmpty { url })
            refreshHistory()
        }
    }

    private suspend fun enforceActiveTabsLimit() {
        val activeTabs = _tabs.value.filter { it.state == TabState.ACTIVE && it.id != _currentTab.value?.id }
            .sortedByDescending { it.lastVisitedAt }

        // Including current tab, active tabs count should be <= maxActiveTabsLimit
        val maxBackgroundActive = (maxActiveTabsLimit - 1).coerceAtLeast(0)
        if (activeTabs.size > maxBackgroundActive) {
            val tabsToPause = activeTabs.drop(maxBackgroundActive)
            for (tab in tabsToPause) {
                pauseTab(tab.id)
            }
        }
    }

    override suspend fun addBookmark(url: String, title: String) {
        repository.addBookmark(url, title)
        refreshBookmarks()
    }

    override suspend fun deleteBookmark(id: Long) {
        repository.deleteBookmark(id)
        refreshBookmarks()
    }

    override suspend fun addHistory(url: String, title: String) {
        repository.addHistory(url, title)
        refreshHistory()
    }

    override suspend fun clearHistory() {
        repository.clearHistory()
        refreshHistory()
    }

    override suspend fun addDownload(name: String, path: String, totalBytes: Long) {
        repository.addDownload(name, path, totalBytes)
        refreshDownloads()
    }

    override suspend fun deleteDownload(id: Long) {
        repository.deleteDownload(id)
        refreshDownloads()
    }

    override fun shouldBlockUrl(url: String): Boolean = contentBlocker.shouldBlock(url)

    override fun trimMemory(level: Int) {
        // Under memory pressure, pause background tabs
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            val currentId = _currentTab.value?.id
            coroutineScope.launch {
                val nonCurrentActive = _tabs.value.filter { it.state == TabState.ACTIVE && it.id != currentId }
                for (tab in nonCurrentActive) {
                    pauseTab(tab.id)
                }
            }
        }
    }

    private suspend fun refreshHistory() {
        _history.value = repository.getHistory()
    }

    private suspend fun refreshBookmarks() {
        _bookmarks.value = repository.getBookmarks()
    }

    private suspend fun refreshDownloads() {
        _downloads.value = repository.getDownloads()
    }
}
