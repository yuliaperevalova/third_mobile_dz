package com.example.third_dz.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.third_dz.data.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private const val DATASTORE_NAME = "ghibli_settings"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {

    object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CACHE_TTL_MINUTES = longPreferencesKey("cache_ttl_minutes")
        val WIFI_ONLY_REFRESH = booleanPreferencesKey("wifi_only_refresh")
        val REFRESH_INTERVAL_HOURS = longPreferencesKey("refresh_interval_hours")
        val DEFAULT_LIST_SORT = stringPreferencesKey("default_list_sort")
        val HISTORY_LIMIT = intPreferencesKey("history_limit")
    }

    object Defaults {
        val THEME_MODE = ThemeMode.SYSTEM
        const val CACHE_TTL_MINUTES = 60L
        const val WIFI_ONLY_REFRESH = false
        const val REFRESH_INTERVAL_HOURS = 24L
        val DEFAULT_LIST_SORT = SortOrder.TITLE_ASC
        const val HISTORY_LIMIT = 50
    }

    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: Defaults.THEME_MODE
    }

    val cacheTtlMinutesFlow: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.CACHE_TTL_MINUTES] ?: Defaults.CACHE_TTL_MINUTES
    }

    val wifiOnlyRefreshFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.WIFI_ONLY_REFRESH] ?: Defaults.WIFI_ONLY_REFRESH
    }

    val refreshIntervalHoursFlow: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.REFRESH_INTERVAL_HOURS] ?: Defaults.REFRESH_INTERVAL_HOURS
    }

    val defaultListSortFlow: Flow<SortOrder> = dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_LIST_SORT]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
            ?: Defaults.DEFAULT_LIST_SORT
    }

    val historyLimitFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.HISTORY_LIMIT] ?: Defaults.HISTORY_LIMIT
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setCacheTtlMinutes(value: Long) {
        dataStore.edit { it[Keys.CACHE_TTL_MINUTES] = value }
    }

    suspend fun setWifiOnlyRefresh(value: Boolean) {
        dataStore.edit { it[Keys.WIFI_ONLY_REFRESH] = value }
    }

    suspend fun setRefreshIntervalHours(value: Long) {
        dataStore.edit { it[Keys.REFRESH_INTERVAL_HOURS] = value }
    }

    suspend fun setDefaultListSort(order: SortOrder) {
        dataStore.edit { it[Keys.DEFAULT_LIST_SORT] = order.name }
    }

    suspend fun setHistoryLimit(value: Int) {
        dataStore.edit { it[Keys.HISTORY_LIMIT] = value }
    }
}
