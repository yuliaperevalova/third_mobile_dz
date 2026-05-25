package com.example.third_dz.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.backup.BackupRepository
import com.example.third_dz.data.local.AppDatabase
import com.example.third_dz.data.model.SortOrder
import com.example.third_dz.data.preferences.SettingsDataStore
import com.example.third_dz.data.preferences.ThemeMode
import com.example.third_dz.data.repository.UniverseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsDataStore,
    private val appDatabase: AppDatabase,
    private val universeRepository: UniverseRepository,
    private val backupRepository: BackupRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val cacheTtlMinutes: StateFlow<Long> = settings.cacheTtlMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.Defaults.CACHE_TTL_MINUTES)

    val wifiOnlyRefresh: StateFlow<Boolean> = settings.wifiOnlyRefreshFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.Defaults.WIFI_ONLY_REFRESH)

    val refreshIntervalHours: StateFlow<Long> = settings.refreshIntervalHoursFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.Defaults.REFRESH_INTERVAL_HOURS)

    val defaultListSort: StateFlow<SortOrder> = settings.defaultListSortFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.Defaults.DEFAULT_LIST_SORT)

    val historyLimit: StateFlow<Int> = settings.historyLimitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.Defaults.HISTORY_LIMIT)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setCacheTtlMinutes(value: Long) {
        viewModelScope.launch { settings.setCacheTtlMinutes(value) }
    }

    fun setWifiOnlyRefresh(value: Boolean) {
        viewModelScope.launch { settings.setWifiOnlyRefresh(value) }
    }

    fun setRefreshIntervalHours(value: Long) {
        viewModelScope.launch { settings.setRefreshIntervalHours(value) }
    }

    fun setDefaultListSort(order: SortOrder) {
        viewModelScope.launch { settings.setDefaultListSort(order) }
    }

    fun setHistoryLimit(value: Int) {
        viewModelScope.launch { settings.setHistoryLimit(value) }
    }

    fun clearAllUserData() {
        viewModelScope.launch { appDatabase.clearAllTables() }
    }

    fun refreshUniverseNow() {
        viewModelScope.launch { universeRepository.refreshAll() }
    }

    fun exportBackup(destinationUri: Uri) {
        viewModelScope.launch {
            val file = backupRepository.export()
            context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                file.inputStream().use { inp -> inp.copyTo(out) }
            }
        }
    }

    fun importBackup(sourceUri: Uri) {
        viewModelScope.launch {
            val tempFile = File(context.cacheDir, "import_temp.json")
            context.contentResolver.openInputStream(sourceUri)?.use { inp ->
                tempFile.outputStream().use { out -> inp.copyTo(out) }
            }
            backupRepository.import(tempFile)
            tempFile.delete()
        }
    }
}
