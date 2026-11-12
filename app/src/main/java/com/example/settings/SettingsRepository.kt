package com.example.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppThemeMode {
    LIGHT,
    DARK,
    AUTO
}

enum class ToolbarPosition {
    BOTTOM,
    TOP
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val accentColorIndex: Int = 0, // 0 = Yellow, 1 = Orange, 2 = Blue Light, 3 = Blue Dark, 4 = Black
    val toolbarPosition: ToolbarPosition = ToolbarPosition.BOTTOM,
    val maxActiveTabs: Int = 3,
    val blockAds: Boolean = true,
    val doNotTrack: Boolean = false,
    val autoHttps: Boolean = true,
    val pauseInactiveTabs: Boolean = true,
    val backupEnabled: Boolean = false
)

private val Context.dataStore by preferencesDataStore(name = "termavigator_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_INDEX = intPreferencesKey("accent_color_index")
        val TOOLBAR_POSITION = stringPreferencesKey("toolbar_position")
        val MAX_ACTIVE_TABS = intPreferencesKey("max_active_tabs")
        val BLOCK_ADS = booleanPreferencesKey("block_ads")
        val DO_NOT_TRACK = booleanPreferencesKey("do_not_track")
        val AUTO_HTTPS = booleanPreferencesKey("auto_https")
        val PAUSE_INACTIVE = booleanPreferencesKey("pause_inactive")
        val BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = runCatching {
                AppThemeMode.valueOf(prefs[PreferencesKeys.THEME_MODE] ?: AppThemeMode.LIGHT.name)
            }.getOrDefault(AppThemeMode.LIGHT),
            accentColorIndex = prefs[PreferencesKeys.ACCENT_COLOR_INDEX] ?: 0,
            toolbarPosition = runCatching {
                ToolbarPosition.valueOf(prefs[PreferencesKeys.TOOLBAR_POSITION] ?: ToolbarPosition.TOP.name)
            }.getOrDefault(ToolbarPosition.TOP),
            maxActiveTabs = prefs[PreferencesKeys.MAX_ACTIVE_TABS] ?: 3,
            blockAds = prefs[PreferencesKeys.BLOCK_ADS] ?: true,
            doNotTrack = prefs[PreferencesKeys.DO_NOT_TRACK] ?: false,
            autoHttps = prefs[PreferencesKeys.AUTO_HTTPS] ?: true,
            pauseInactiveTabs = prefs[PreferencesKeys.PAUSE_INACTIVE] ?: true,
            backupEnabled = prefs[PreferencesKeys.BACKUP_ENABLED] ?: false
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColorIndex(index: Int) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_COLOR_INDEX] = index }
    }

    suspend fun setToolbarPosition(position: ToolbarPosition) {
        context.dataStore.edit { it[PreferencesKeys.TOOLBAR_POSITION] = position.name }
    }

    suspend fun setMaxActiveTabs(count: Int) {
        context.dataStore.edit { it[PreferencesKeys.MAX_ACTIVE_TABS] = count }
    }

    suspend fun setBlockAds(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BLOCK_ADS] = enabled }
    }

    suspend fun setDoNotTrack(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DO_NOT_TRACK] = enabled }
    }

    suspend fun setAutoHttps(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_HTTPS] = enabled }
    }

    suspend fun setPauseInactiveTabs(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PAUSE_INACTIVE] = enabled }
    }

    suspend fun setBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BACKUP_ENABLED] = enabled }
    }
}
