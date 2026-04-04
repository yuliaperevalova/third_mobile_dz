package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
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
import kotlinx.coroutines.launch
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
class FilmsListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mockk<GhibliFilmsRepository>()
    private val mockDao = mockk<FavouriteFilmDao>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun emptyFavouritesFlow() = flowOf<List<FavouriteFilmEntity>>(emptyList())

    // Test 1: начальное состояние — Loading до того, как отработают корутины
    @Test
    fun initialState_isLoading() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(listOf(makeFilm()))
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockDao)

        // StandardTestDispatcher не запускает корутины автоматически —
        // проверяем состояние до вызова advanceUntilIdle()
        assertEquals(FilmsListUiState.Loading, vm.uiState.value)
    }

    // Test 2: успешная загрузка → Success с правильным списком фильмов
    @Test
    fun loadFilms_success_emitsSuccessState() = runTest(testDispatcher) {
        val films = listOf(makeFilm("1"), makeFilm("2"))
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(films)
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockDao)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Success)
        assertEquals(films, (state as FilmsListUiState.Success).films)
        job.cancel()
    }

    // Test 3: пустой список → Empty, а не Success(emptyList())
    @Test
    fun loadFilms_emptyList_emitsEmptyState() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockDao)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(FilmsListUiState.Empty, vm.uiState.value)
        job.cancel()
    }

    // Test 4: ошибка загрузки → Error с сообщением
    @Test
    fun loadFilms_error_emitsErrorState() = runTest(testDispatcher) {
        val errorMessage = "connection timeout"
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } throws RuntimeException(errorMessage)

        val vm = FilmsListViewModel(mockRepository, mockDao)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Error)
        assertEquals(errorMessage, (state as FilmsListUiState.Error).message)
        job.cancel()
    }

    // Test Flow-1 (Turbine): полная последовательность эмиссий Loading → Success
    @Test
    fun loadFilms_emitsLoadingThenSuccess() = runTest(testDispatcher) {
        val films = listOf(makeFilm())
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        every { mockRepository.getFilmsFlow() } returns flowOf(films)
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = FilmsListViewModel(mockRepository, mockDao)

        vm.uiState.test {
            assertEquals(FilmsListUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is FilmsListUiState.Success)
            assertEquals(films, (success as FilmsListUiState.Success).films)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test Flow-2: обновление избранного в Error состоянии не порождает лишней эмиссии
    @Test
    fun favouritesUpdate_whileInErrorState_doesNotEmitExtraState() = runTest(testDispatcher) {
        val favouritesFlow = MutableStateFlow<List<FavouriteFilmEntity>>(emptyList())

        every { mockDao.getAllFavourites() } returns favouritesFlow
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = FilmsListViewModel(mockRepository, mockDao)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(FilmsListUiState.Error("network error"), awaitItem())

            // Эмитим обновление избранного пока VM в Error — стейт не меняется
            favouritesFlow.value = emptyList()
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        job.cancel()
    }

    // Test 5: retry после ошибки вызывает refreshFilms() повторно
    @Test
    fun retry_afterError_callsRefreshAgain() = runTest(testDispatcher) {
        val filmsFlow = MutableStateFlow<List<Film>>(emptyList())
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        every { mockRepository.getFilmsFlow() } returns filmsFlow
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = FilmsListViewModel(mockRepository, mockDao)
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
