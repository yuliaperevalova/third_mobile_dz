package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.local.AppDatabase
import com.example.third_dz.data.model.SortOrder
import com.example.third_dz.data.preferences.SettingsDataStore
import com.example.third_dz.data.preferences.ThemeMode
import com.example.third_dz.data.repository.UniverseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockSettings = mockk<SettingsDataStore>(relaxed = true)
    private val mockDb = mockk<AppDatabase>(relaxed = true)
    private val mockUniverseRepo = mockk<UniverseRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockSettings.themeModeFlow } returns flowOf(ThemeMode.SYSTEM)
        every { mockSettings.cacheTtlMinutesFlow } returns flowOf(60L)
        every { mockSettings.wifiOnlyRefreshFlow } returns flowOf(false)
        every { mockSettings.refreshIntervalHoursFlow } returns flowOf(24L)
        every { mockSettings.defaultListSortFlow } returns flowOf(SortOrder.TITLE_ASC)
        every { mockSettings.historyLimitFlow } returns flowOf(50)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun themeMode_initialValue() = runTest(testDispatcher) {
        val vm = SettingsViewModel(mockSettings, mockDb, mockUniverseRepo)

        vm.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setThemeMode_updatesState() = runTest(testDispatcher) {
        val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
        every { mockSettings.themeModeFlow } returns themeModeFlow
        coEvery { mockSettings.setThemeMode(any()) } answers { themeModeFlow.value = firstArg<ThemeMode>() }

        val vm = SettingsViewModel(mockSettings, mockDb, mockUniverseRepo)

        vm.themeMode.test {
            awaitItem()
            vm.setThemeMode(ThemeMode.DARK)
            advanceUntilIdle()
            assertEquals(ThemeMode.DARK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAllUserData_callsClearAllTables() = runTest(testDispatcher) {
        val vm = SettingsViewModel(mockSettings, mockDb, mockUniverseRepo)
        vm.clearAllUserData()
        advanceUntilIdle()

        coVerify { mockDb.clearAllTables() }
    }

    @Test
    fun refreshUniverseNow_callsRefreshAll() = runTest(testDispatcher) {
        val vm = SettingsViewModel(mockSettings, mockDb, mockUniverseRepo)
        vm.refreshUniverseNow()
        advanceUntilIdle()

        coVerify { mockUniverseRepo.refreshAll() }
    }

    @Test
    fun allSettings_haveInitialValues() = runTest(testDispatcher) {
        val vm = SettingsViewModel(mockSettings, mockDb, mockUniverseRepo)

        vm.cacheTtlMinutes.test {
            assertEquals(60L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.wifiOnlyRefresh.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.refreshIntervalHours.test {
            assertEquals(24L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.defaultListSort.test {
            assertEquals(SortOrder.TITLE_ASC, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.historyLimit.test {
            assertEquals(50, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
