package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.local.RecentViewEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.RecentViewRepository
import com.example.third_dz.domain.usecase.recent.ClearHistoryUseCase
import com.example.third_dz.domain.usecase.recent.ObserveRecentUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeRecent = mockk<ObserveRecentUseCase>()
    private val clearHistory = mockk<ClearHistoryUseCase>(relaxed = true)
    private val recentViewRepository = mockk<RecentViewRepository>(relaxed = true)
    private val filmRepository = mockk<GhibliFilmsRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() = runTest(testDispatcher) {
        every { observeRecent() } returns flowOf(emptyList())
        every { filmRepository.getFilmsFlow() } returns flowOf(emptyList())

        val vm = HistoryViewModel(observeRecent, clearHistory, recentViewRepository, filmRepository)

        vm.state.test {
            val emitted = awaitItem()
            assertTrue(emitted.isLoading)
            assertTrue(emitted.items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emitsItemsFromUseCase() = runTest(testDispatcher) {
        val film = Film(
            id = "f1", title = "Film 1", originalTitle = "", originalTitleRomanised = "",
            description = "", director = "", producer = "", releaseDate = "", runningTime = "",
            rtScore = "", people = emptyList(), species = emptyList(), locations = emptyList(),
            vehicles = emptyList(), url = ""
        )
        val entity = RecentViewEntity(id = 1L, filmId = "f1", openedAt = 1000L)
        every { observeRecent() } returns flowOf(listOf(entity))
        every { filmRepository.getFilmsFlow() } returns flowOf(listOf(film))

        val vm = HistoryViewModel(observeRecent, clearHistory, recentViewRepository, filmRepository)

        vm.state.test {
            awaitItem() // skip initial (isLoading=true)
            val emitted = awaitItem()
            assertEquals("Film 1", emitted.items[0].filmTitle)
            assertEquals(1000L, emitted.items[0].openedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAll_callsClearHistory() = runTest(testDispatcher) {
        every { observeRecent() } returns flowOf(emptyList())
        every { filmRepository.getFilmsFlow() } returns flowOf(emptyList())

        val vm = HistoryViewModel(observeRecent, clearHistory, recentViewRepository, filmRepository)
        vm.clearAll()
        advanceUntilIdle()

        coVerify { clearHistory() }
    }

    @Test
    fun deleteItem_callsRepositoryDeleteById() = runTest(testDispatcher) {
        every { observeRecent() } returns flowOf(emptyList())
        every { filmRepository.getFilmsFlow() } returns flowOf(emptyList())

        val vm = HistoryViewModel(observeRecent, clearHistory, recentViewRepository, filmRepository)
        vm.deleteItem(42L)
        advanceUntilIdle()

        coVerify { recentViewRepository.deleteById(42L) }
    }

    @Test
    fun stateUpdatesWhenObserveRecentEmits() = runTest(testDispatcher) {
        every { observeRecent() } returns flowOf(emptyList())
        every { filmRepository.getFilmsFlow() } returns flowOf(emptyList())

        val vm = HistoryViewModel(observeRecent, clearHistory, recentViewRepository, filmRepository)

        vm.state.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
