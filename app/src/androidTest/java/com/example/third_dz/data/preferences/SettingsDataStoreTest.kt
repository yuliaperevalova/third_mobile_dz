package com.example.third_dz.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import com.example.third_dz.data.model.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class SettingsDataStoreTest {

    private lateinit var tmpFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var settings: SettingsDataStore

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        tmpFile = File.createTempFile("settings_test", ".preferences_pb", ctx.cacheDir)
        tmpFile.delete()
        dataStore = PreferenceDataStoreFactory.create(produceFile = { tmpFile })
        settings = SettingsDataStore(dataStore)
    }

    @After
    fun tearDown() {
        tmpFile.delete()
    }

    @Test
    fun defaults_areReturned_whenNothingSaved() = runBlocking {
        assertEquals(SettingsDataStore.Defaults.THEME_MODE, settings.themeModeFlow.first())
        assertEquals(SettingsDataStore.Defaults.CACHE_TTL_MINUTES, settings.cacheTtlMinutesFlow.first())
        assertEquals(SettingsDataStore.Defaults.WIFI_ONLY_REFRESH, settings.wifiOnlyRefreshFlow.first())
        assertEquals(SettingsDataStore.Defaults.REFRESH_INTERVAL_HOURS, settings.refreshIntervalHoursFlow.first())
        assertEquals(SettingsDataStore.Defaults.DEFAULT_LIST_SORT, settings.defaultListSortFlow.first())
        assertEquals(SettingsDataStore.Defaults.HISTORY_LIMIT, settings.historyLimitFlow.first())
    }

    @Test
    fun setThemeMode_persists() = runBlocking {
        settings.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, settings.themeModeFlow.first())
    }

    @Test
    fun setCacheTtlAndHistoryLimit_persist() = runBlocking {
        settings.setCacheTtlMinutes(180L)
        settings.setHistoryLimit(25)
        assertEquals(180L, settings.cacheTtlMinutesFlow.first())
        assertEquals(25, settings.historyLimitFlow.first())
    }

    @Test
    fun setDefaultListSort_persists() = runBlocking {
        settings.setDefaultListSort(SortOrder.YEAR_DESC)
        assertEquals(SortOrder.YEAR_DESC, settings.defaultListSortFlow.first())
    }

    @Test
    fun setWifiOnlyAndRefreshInterval_persist() = runBlocking {
        settings.setWifiOnlyRefresh(true)
        settings.setRefreshIntervalHours(12L)
        assertEquals(true, settings.wifiOnlyRefreshFlow.first())
        assertEquals(12L, settings.refreshIntervalHoursFlow.first())
    }
}
