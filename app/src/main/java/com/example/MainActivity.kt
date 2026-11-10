package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.blocking.ContentBlocker
import com.example.core.KotlinBrowserCore
import com.example.core.models.TabItem
import com.example.core.models.TabState
import com.example.settings.AppSettings
import com.example.settings.AppThemeMode
import com.example.settings.SettingsRepository
import com.example.settings.ToolbarPosition
import com.example.storage.BrowserRepository
import com.example.ui.AppearanceScreen
import com.example.ui.ArchivedTabsScreen
import com.example.ui.BookmarksScreen
import com.example.ui.BrowserScreen
import com.example.ui.DownloadsScreen
import com.example.ui.HistoryScreen
import com.example.ui.MemoryScreen
import com.example.ui.MenuBottomSheet
import com.example.ui.NewTabScreen
import com.example.ui.PrivacyScreen
import com.example.ui.PrivateScreen
import com.example.ui.SearchInputDialog
import com.example.ui.SettingsScreen
import com.example.ui.TabsScreen
import com.example.ui.backup.BackupScreen
import com.example.ui.backup.CreatePasswordScreen
import com.example.ui.backup.ManageBackupScreen
import com.example.ui.backup.RestoreScreen
import com.example.ui.theme.TermavigatorBlack
import com.example.ui.theme.TermavigatorBlueDark
import com.example.ui.theme.TermavigatorBlueLight
import com.example.ui.theme.TermavigatorOrange
import com.example.ui.theme.TermavigatorTheme
import com.example.ui.theme.TermavigatorYellow
import kotlinx.coroutines.launch

enum class Screen {
    BROWSER,
    NEW_TAB,
    TABS_MANAGER,
    ARCHIVED_TABS,
    HISTORY,
    BOOKMARKS,
    DOWNLOADS,
    SETTINGS,
    APPEARANCE,
    PRIVACY,
    MEMORY,
    PRIVATE_BROWSER,
    BACKUP,
    BACKUP_PASSWORD,
    BACKUP_RESTORE,
    BACKUP_MANAGE
}

class MainActivity : ComponentActivity() {

    private lateinit var browserCore: KotlinBrowserCore
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var contentBlocker: ContentBlocker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = BrowserRepository(applicationContext)
        contentBlocker = ContentBlocker(enabled = true)
        browserCore = KotlinBrowserCore(
            repository = repository,
            contentBlocker = contentBlocker,
            coroutineScope = lifecycleScope
        )
        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
            val tabs by browserCore.tabsState.collectAsState()
            val currentTab by browserCore.currentTabState.collectAsState()
            val history by browserCore.historyState.collectAsState()
            val bookmarks by browserCore.bookmarksState.collectAsState()
            val downloads by browserCore.downloadsState.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (settings.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.AUTO -> isSystemDark
            }

            val accentColors = listOf(
                TermavigatorYellow,
                TermavigatorOrange,
                TermavigatorBlueLight,
                TermavigatorBlueDark,
                TermavigatorBlack
            )
            val selectedAccent = accentColors.getOrElse(settings.accentColorIndex) { TermavigatorYellow }

            TermavigatorTheme(
                darkTheme = isDarkTheme,
                accentColor = selectedAccent
            ) {
                TermavigatorApp(
                    browserCore = browserCore,
                    settingsRepository = settingsRepository,
                    contentBlocker = contentBlocker,
                    settings = settings,
                    tabs = tabs,
                    currentTab = currentTab,
                    history = history,
                    bookmarks = bookmarks,
                    downloads = downloads,
                    onShareUrl = { url -> shareUrl(url) },
                    onOpenFile = { path -> openDownloadedFile(path) }
                )
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (::browserCore.isInitialized) {
            browserCore.trimMemory(level)
        }
    }

    private fun shareUrl(url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartilhar link"))
    }

    private fun openDownloadedFile(path: String) {
        Toast.makeText(this, "Arquivo pronto: $path", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun TermavigatorApp(
    browserCore: KotlinBrowserCore,
    settingsRepository: SettingsRepository,
    contentBlocker: ContentBlocker,
    settings: AppSettings,
    tabs: List<TabItem>,
    currentTab: TabItem?,
    history: List<com.example.core.models.HistoryItem>,
    bookmarks: List<com.example.core.models.BookmarkItem>,
    downloads: List<com.example.core.models.DownloadItem>,
    onShareUrl: (String) -> Unit,
    onOpenFile: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.BROWSER) }
    var screenStack by remember { mutableStateOf(listOf(Screen.BROWSER)) }
    var showMenu by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    fun navigateTo(screen: Screen) {
        screenStack = screenStack + screen
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
            currentScreen = screenStack.last()
        } else {
            currentScreen = Screen.BROWSER
        }
    }

    // Hardware and gesture BackHandler
    BackHandler(enabled = currentScreen != Screen.BROWSER) {
        navigateBack()
    }

    // Keep core active tab limit in sync with settings
    browserCore.maxActiveTabsLimit = settings.maxActiveTabs
    contentBlocker.setEnabled(settings.blockAds)

    val isToolbarAtTop = settings.toolbarPosition == ToolbarPosition.TOP

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                Screen.BROWSER -> {
                    BrowserScreen(
                        currentTab = currentTab,
                        tabCount = tabs.size,
                        contentBlocker = contentBlocker,
                        isToolbarAtTop = isToolbarAtTop,
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true },
                        onPageLoaded = { url, title ->
                            coroutineScope.launch {
                                browserCore.updateCurrentTab(url, title)
                            }
                        }
                    )
                }
                Screen.NEW_TAB -> {
                    NewTabScreen(
                        tabCount = tabs.size,
                        recentBookmarks = bookmarks,
                        isToolbarAtTop = isToolbarAtTop,
                        onSearchClick = { showSearchDialog = true },
                        onNavigateUrl = { targetUrl ->
                            coroutineScope.launch {
                                browserCore.createTab(targetUrl)
                                currentScreen = Screen.BROWSER
                            }
                        },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true }
                    )
                }
                Screen.TABS_MANAGER -> {
                    TabsScreen(
                        tabs = tabs,
                        currentTabId = currentTab?.id,
                        isToolbarAtTop = isToolbarAtTop,
                        onSelectTab = { tabId ->
                            coroutineScope.launch {
                                browserCore.selectTab(tabId)
                                currentScreen = Screen.BROWSER
                            }
                        },
                        onCloseTab = { tabId ->
                            coroutineScope.launch {
                                browserCore.closeTab(tabId)
                            }
                        },
                        onArchiveTab = { tabId ->
                            coroutineScope.launch {
                                browserCore.archiveTab(tabId)
                            }
                        },
                        onNewTabClick = {
                            coroutineScope.launch {
                                browserCore.createTab()
                                currentScreen = Screen.NEW_TAB
                            }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onMenuClick = { showMenu = true },
                        onArchivedTabsClick = { navigateTo(Screen.ARCHIVED_TABS) }
                    )
                }
                Screen.ARCHIVED_TABS -> {
                    ArchivedTabsScreen(
                        archivedTabs = tabs.filter { it.state == TabState.ARCHIVED },
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onRestoreTab = { tabId ->
                            coroutineScope.launch {
                                browserCore.restoreArchivedTab(tabId)
                                currentScreen = Screen.BROWSER
                            }
                        },
                        onDeleteTab = { tabId ->
                            coroutineScope.launch {
                                browserCore.closeTab(tabId)
                            }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true }
                    )
                }
                Screen.HISTORY -> {
                    HistoryScreen(
                        historyList = history,
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onItemClick = { url ->
                            coroutineScope.launch {
                                browserCore.createTab(url)
                                currentScreen = Screen.BROWSER
                            }
                        },
                        onClearHistory = {
                            coroutineScope.launch {
                                browserCore.clearHistory()
                            }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true }
                    )
                }
                Screen.BOOKMARKS -> {
                    BookmarksScreen(
                        bookmarks = bookmarks,
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onBookmarkClick = { url ->
                            coroutineScope.launch {
                                browserCore.createTab(url)
                                currentScreen = Screen.BROWSER
                            }
                        },
                        onAddBookmarkClick = {
                            currentTab?.let { tab ->
                                coroutineScope.launch {
                                    browserCore.addBookmark(tab.url, tab.title)
                                }
                            }
                        },
                        onDeleteBookmark = { id ->
                            coroutineScope.launch {
                                browserCore.deleteBookmark(id)
                            }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true }
                    )
                }
                Screen.DOWNLOADS -> {
                    DownloadsScreen(
                        downloads = downloads,
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onOpenFile = { item -> onOpenFile(item.path) },
                        onRetryDownload = { item ->
                            coroutineScope.launch {
                                browserCore.addDownload(item.name, item.path, item.totalBytes)
                            }
                        },
                        onCancelDownload = { id ->
                            coroutineScope.launch {
                                browserCore.deleteDownload(id)
                            }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true }
                    )
                }
                Screen.SETTINGS -> {
                    SettingsScreen(
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onGeneralClick = { navigateTo(Screen.APPEARANCE) },
                        onPrivacyClick = { navigateTo(Screen.PRIVACY) },
                        onAppearanceClick = { navigateTo(Screen.APPEARANCE) },
                        onMemoryClick = { navigateTo(Screen.MEMORY) },
                        onAboutClick = { navigateTo(Screen.MEMORY) },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) }
                    )
                }
                Screen.APPEARANCE -> {
                    AppearanceScreen(
                        currentTheme = settings.themeMode,
                        currentAccentIndex = settings.accentColorIndex,
                        currentToolbarPosition = settings.toolbarPosition,
                        tabCount = tabs.size,
                        onBackClick = { navigateBack() },
                        onThemeChanged = { mode ->
                            coroutineScope.launch { settingsRepository.setThemeMode(mode) }
                        },
                        onAccentChanged = { index ->
                            coroutineScope.launch { settingsRepository.setAccentColorIndex(index) }
                        },
                        onToolbarPositionChanged = { position ->
                            coroutineScope.launch { settingsRepository.setToolbarPosition(position) }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) }
                    )
                }
                Screen.PRIVACY -> {
                    PrivacyScreen(
                        blockAds = settings.blockAds,
                        doNotTrack = settings.doNotTrack,
                        autoHttps = settings.autoHttps,
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onBlockAdsChanged = { enabled ->
                            coroutineScope.launch { settingsRepository.setBlockAds(enabled) }
                        },
                        onDoNotTrackChanged = { enabled ->
                            coroutineScope.launch { settingsRepository.setDoNotTrack(enabled) }
                        },
                        onAutoHttpsChanged = { enabled ->
                            coroutineScope.launch { settingsRepository.setAutoHttps(enabled) }
                        },
                        onClearBrowsingData = {
                            coroutineScope.launch { browserCore.clearHistory() }
                        },
                        onClearCookies = {
                            android.webkit.CookieManager.getInstance().removeAllCookies(null)
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) }
                    )
                }
                Screen.MEMORY -> {
                    MemoryScreen(
                        maxActiveTabs = settings.maxActiveTabs,
                        pauseInactiveTabs = settings.pauseInactiveTabs,
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onBackClick = { navigateBack() },
                        onMaxActiveTabsChanged = { count ->
                            coroutineScope.launch { settingsRepository.setMaxActiveTabs(count) }
                        },
                        onPauseInactiveTabsChanged = { enabled ->
                            coroutineScope.launch { settingsRepository.setPauseInactiveTabs(enabled) }
                        },
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) }
                    )
                }
                Screen.PRIVATE_BROWSER -> {
                    PrivateScreen(
                        currentTab = currentTab,
                        tabCount = tabs.size,
                        isToolbarAtTop = isToolbarAtTop,
                        onUrlClick = { showSearchDialog = true },
                        onTabsClick = { navigateTo(Screen.TABS_MANAGER) },
                        onMenuClick = { showMenu = true }
                    )
                }
                Screen.BACKUP -> {
                    BackupScreen(onBackClick = { navigateBack() })
                }
                Screen.BACKUP_PASSWORD -> {
                    CreatePasswordScreen(onBackClick = { navigateBack() })
                }
                Screen.BACKUP_RESTORE -> {
                    RestoreScreen(onBackClick = { navigateBack() })
                }
                Screen.BACKUP_MANAGE -> {
                    ManageBackupScreen(onBackClick = { navigateBack() })
                }
            }
        }
    }

    // T05 · Menu Bottom Sheet
    if (showMenu) {
        MenuBottomSheet(
            onDismiss = { showMenu = false },
            onNewTab = {
                coroutineScope.launch {
                    browserCore.createTab()
                    currentScreen = Screen.NEW_TAB
                }
            },
            onNewPrivateTab = {
                coroutineScope.launch {
                    browserCore.createTab(isPrivate = true)
                    currentScreen = Screen.PRIVATE_BROWSER
                }
            },
            onHistoryClick = { navigateTo(Screen.HISTORY) },
            onBookmarksClick = { navigateTo(Screen.BOOKMARKS) },
            onDownloadsClick = { navigateTo(Screen.DOWNLOADS) },
            onAddToBookmarks = {
                currentTab?.let { tab ->
                    coroutineScope.launch {
                        browserCore.addBookmark(tab.url, tab.title)
                    }
                }
            },
            onShare = {
                currentTab?.url?.let { onShareUrl(it) }
            },
            onSettingsClick = { navigateTo(Screen.SETTINGS) }
        )
    }

    // Search / URL Input Dialog
    if (showSearchDialog) {
        SearchInputDialog(
            initialText = currentTab?.url.orEmpty(),
            onDismiss = { showSearchDialog = false },
            onSubmit = { enteredUrl ->
                coroutineScope.launch {
                    val normalized = browserCore.normalizeInput(enteredUrl)
                    if (currentScreen == Screen.PRIVATE_BROWSER) {
                        browserCore.createTab(normalized, isPrivate = true)
                    } else {
                        browserCore.createTab(normalized, isPrivate = false)
                        currentScreen = Screen.BROWSER
                    }
                }
            }
        )
    }
}
