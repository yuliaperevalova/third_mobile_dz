package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.PinnedRepository
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import com.example.third_dz.util.makeFilm
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class FilmsListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mockk<GhibliFilmsRepository>()
    private val mockRecordRepository = mockk<UserFilmRecordRepository>()
    private val mockPinnedRepository = mockk<PinnedRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun emptyRecordsFlow() = flowOf<List<UserFilmRecord>>(emptyList())

    @Test
    fun initialState_isLoading() = runTest(testDispatcher) {
        every { mockRecordRepository.observeAll() } returns emptyRecordsFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(listOf(makeFilm()))
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)

        assertEquals(FilmsListUiState.Loading, vm.uiState.value)
    }

    @Test
    fun loadFilms_success_emitsSuccessState() = runTest(testDispatcher) {
        val films = listOf(makeFilm("1"), makeFilm("2"))
        every { mockRecordRepository.observeAll() } returns emptyRecordsFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(films)
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Success)
        assertEquals(films, (state as FilmsListUiState.Success).films)
        job.cancel()
    }

    @Test
    fun loadFilms_emptyList_emitsEmptyState() = runTest(testDispatcher) {
        every { mockRecordRepository.observeAll() } returns emptyRecordsFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(FilmsListUiState.Empty, vm.uiState.value)
        job.cancel()
    }

    @Test
    fun loadFilms_error_emitsErrorState() = runTest(testDispatcher) {
        val msg = "connection timeout"
        every { mockRecordRepository.observeAll() } returns emptyRecordsFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } throws RuntimeException(msg)

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Error)
        assertEquals(msg, (state as FilmsListUiState.Error).message)
        job.cancel()
    }

    @Test
    fun loadFilms_emitsLoadingThenSuccess() = runTest(testDispatcher) {
        val films = listOf(makeFilm())
        every { mockRecordRepository.observeAll() } returns emptyRecordsFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(films)
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)

        vm.uiState.test {
            assertEquals(FilmsListUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is FilmsListUiState.Success)
            assertEquals(films, (success as FilmsListUiState.Success).films)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun statusFilter_filtersFilmsByRecordStatus() = runTest(testDispatcher) {
        val films = listOf(makeFilm("1"), makeFilm("2"))
        val records = listOf(
            UserFilmRecord("1", WatchStatus.WATCHED, null, null, null, 0L),
            UserFilmRecord("2", WatchStatus.PLAN, null, null, null, 0L)
        )
        every { mockRecordRepository.observeAll() } returns flowOf(records)
        every { mockRepository.getFilmsFlow() } returns flowOf(films)
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FilmsListEvent.StatusFilterChanged(WatchStatus.WATCHED))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Success)
        val success = state as FilmsListUiState.Success
        assertEquals(1, success.films.size)
        assertEquals("1", success.films[0].id)
        job.cancel()
    }

    @Test
    fun retry_afterError_callsRefreshAgain() = runTest(testDispatcher) {
        val filmsFlow = MutableStateFlow<List<Film>>(emptyList())
        every { mockRecordRepository.observeAll() } returns emptyRecordsFlow()
        every { mockRepository.getFilmsFlow() } returns filmsFlow
        every { mockPinnedRepository.observeAll() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = FilmsListViewModel(mockRepository, mockRecordRepository, mockPinnedRepository)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(vm.uiState.value is FilmsListUiState.Error)

        coEvery { mockRepository.refreshFilms() } answers { filmsFlow.value = listOf(makeFilm()) }
        vm.onEvent(FilmsListEvent.Refresh)
        advanceUntilIdle()

        coVerify(exactly = 2) { mockRepository.refreshFilms() }
        assertTrue(vm.uiState.value is FilmsListUiState.Success)
        job.cancel()
    }
}
