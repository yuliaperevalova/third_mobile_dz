package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import io.mockk.coEvery
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

    private fun makeFilm(id: String = "1") = Film(
        id = id,
        title = "Princess Mononoke",
        original_title = "もののけ姫",
        original_title_romanised = "Mononoke Hime",
        description = "A young man raised by wolves",
        director = "Hayao Miyazaki",
        producer = "Toshio Suzuki",
        release_date = "1997",
        running_time = "133",
        rt_score = "92",
        people = emptyList(),
        species = emptyList(),
        locations = emptyList(),
        vehicles = emptyList(),
        url = "https://ghibliapi.vercel.app/films/$id"
    )

    // Test 1: начальное состояние — Loading до того, как отработают корутины
    @Test
    fun initialState_isLoading() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        coEvery { mockRepository.getAllFilms(any()) } returns listOf(makeFilm())

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
        coEvery { mockRepository.getAllFilms(any()) } returns films

        val vm = FilmsListViewModel(mockRepository, mockDao)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Success)
        assertEquals(films, (state as FilmsListUiState.Success).films)
    }

    // Test 3 (нетривиальный): пустой список → Empty, а не Success(emptyList())
    @Test
    fun loadFilms_emptyList_emitsEmptyState() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        coEvery { mockRepository.getAllFilms(any()) } returns emptyList()

        val vm = FilmsListViewModel(mockRepository, mockDao)
        advanceUntilIdle()

        assertEquals(FilmsListUiState.Empty, vm.uiState.value)
    }

    // Test 4: ошибка загрузки → Error с сообщением
    @Test
    fun loadFilms_error_emitsErrorState() = runTest(testDispatcher) {
        val errorMessage = "connection timeout"
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        coEvery { mockRepository.getAllFilms(any()) } throws RuntimeException(errorMessage)

        val vm = FilmsListViewModel(mockRepository, mockDao)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FilmsListUiState.Error)
        assertEquals(errorMessage, (state as FilmsListUiState.Error).message)
    }

    // Test Flow-1 (Turbine): полная последовательность эмиссий Loading → Success
    @Test
    fun loadFilms_emitsLoadingThenSuccess() = runTest(testDispatcher) {
        val films = listOf(makeFilm())
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        coEvery { mockRepository.getAllFilms(any()) } returns films

        val vm = FilmsListViewModel(mockRepository, mockDao)

        vm.uiState.test {
            // StateFlow немедленно эмитит текущее значение при подписке
            assertEquals(FilmsListUiState.Loading, awaitItem())
            // Запускаем корутины — loadFilms() отрабатывает и обновляет state
            testDispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is FilmsListUiState.Success)
            assertEquals(films, (success as FilmsListUiState.Success).films)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test 5 (нетривиальный): retry после ошибки действительно инициирует новый запрос к API
    @Test
    fun retry_afterError_callsApiAgain() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns emptyFavouritesFlow()
        coEvery { mockRepository.getAllFilms(any()) } throws RuntimeException("network error")

        val vm = FilmsListViewModel(mockRepository, mockDao)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is FilmsListUiState.Error)

        coEvery { mockRepository.getAllFilms(any()) } returns listOf(makeFilm())
        vm.onEvent(FilmsListEvent.Refresh)
        advanceUntilIdle()

        // Проверяем контракт: API вызван ровно дважды — первый раз при init, второй при retry
        coVerify(exactly = 2) { mockRepository.getAllFilms(any()) }
        assertTrue(vm.uiState.value is FilmsListUiState.Success)
    }
}
