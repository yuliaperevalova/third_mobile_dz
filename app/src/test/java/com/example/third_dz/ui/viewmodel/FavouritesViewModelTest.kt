package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.FavouritesRepository
import com.example.third_dz.ui.event.FavouritesEvent
import com.example.third_dz.ui.state.FavouritesUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
class FavouritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository = mockk<FavouritesRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeFilm(id: String = "1", title: String = "My Neighbor Totoro") = Film(
        id = id,
        title = title,
        original_title = "となりのトトロ",
        original_title_romanised = "Tonari no Totoro",
        description = "Two sisters move to the country",
        director = "Hayao Miyazaki",
        producer = "Toshio Suzuki",
        release_date = "1988",
        running_time = "86",
        rt_score = "93",
        people = emptyList(),
        species = emptyList(),
        locations = emptyList(),
        vehicles = emptyList(),
        url = "https://ghibliapi.vercel.app/films/$id"
    )

    @Test
    fun initialState_withEmptyDb_emitsEmpty() = runTest(testDispatcher) {
        every { mockRepository.getAllFavourites() } returns flowOf(emptyList())

        val vm = FavouritesViewModel(mockRepository)

        val collected = mutableListOf<FavouritesUiState>()
        val job = launch { vm.uiState.collect { collected.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(collected.contains(FavouritesUiState.Empty))
    }

    @Test
    fun retry_cancelsStaleFlow_staleResultNeverReachesUiState() = runTest(testDispatcher) {
        val staleChannel = Channel<List<Film>>(Channel.BUFFERED)
        val freshChannel = Channel<List<Film>>(Channel.UNLIMITED)
        freshChannel.send(emptyList())

        var callCount = 0
        every { mockRepository.getAllFavourites() } answers {
            callCount++
            if (callCount == 1) staleChannel.receiveAsFlow()
            else freshChannel.receiveAsFlow()
        }

        val vm = FavouritesViewModel(mockRepository)

        vm.uiState.test {
            assertEquals(FavouritesUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(FavouritesEvent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(FavouritesUiState.Empty, awaitItem())

            val staleFilms = listOf(makeFilm("stale"))
            staleChannel.send(staleFilms)
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleFavourite_updatesStateAndEmitsRemovedEvent() = runTest(testDispatcher) {
        val film = makeFilm("1")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(film))

        every { mockRepository.getAllFavourites() } returns favouritesFlow
        coEvery { mockRepository.removeFavourite(any()) } returns Unit

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.filmRemovedEvent.test {
            vm.onEvent(FavouritesEvent.ToggleFavourite("1"))
            favouritesFlow.emit(emptyList())
            advanceUntilIdle()

            assertTrue(vm.uiState.value is FavouritesUiState.Empty)
            assertEquals("1", awaitItem())
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }

        stateJob.cancel()
    }

    @Test
    fun filmRemovedEvent_newSubscriberDoesNotReceivePastEvent() = runTest(testDispatcher) {
        val film = makeFilm("1")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(film))

        every { mockRepository.getAllFavourites() } returns favouritesFlow
        coEvery { mockRepository.removeFavourite(any()) } returns Unit

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.ToggleFavourite("1"))
        advanceUntilIdle()

        stateJob.cancel()

        vm.filmRemovedEvent.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retry_triggersNewFlatMapLatestSubscription() = runTest(testDispatcher) {
        every { mockRepository.getAllFavourites() } returns flowOf(emptyList())

        val vm = FavouritesViewModel(mockRepository)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.Retry)
        advanceUntilIdle()

        job.cancel()

        verify(exactly = 2) { mockRepository.getAllFavourites() }
    }

    @Test
    fun searchQuery_filtersFilmsByTitle() = runTest(testDispatcher) {
        val totoro = makeFilm("1", "My Neighbor Totoro")
        val spirited = makeFilm("2", "Spirited Away")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(totoro, spirited))

        every { mockRepository.getAllFavourites() } returns favouritesFlow

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.SearchQueryChanged("Spirited"))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FavouritesUiState.Success)
        val films = (state as FavouritesUiState.Success).films
        assertEquals(1, films.size)
        assertEquals("Spirited Away", films[0].title)

        stateJob.cancel()
    }

    @Test
    fun searchQuery_caseInsensitive() = runTest(testDispatcher) {
        val totoro = makeFilm("1", "My Neighbor Totoro")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(totoro))

        every { mockRepository.getAllFavourites() } returns favouritesFlow

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.SearchQueryChanged("totoro"))
        advanceUntilIdle()

        assertTrue(vm.uiState.value is FavouritesUiState.Success)

        stateJob.cancel()
    }

    @Test
    fun searchQuery_noMatch_emitsEmpty() = runTest(testDispatcher) {
        val totoro = makeFilm("1", "My Neighbor Totoro")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(totoro))

        every { mockRepository.getAllFavourites() } returns favouritesFlow

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.SearchQueryChanged("zzz"))
        advanceUntilIdle()

        assertEquals(FavouritesUiState.Empty, vm.uiState.value)

        stateJob.cancel()
    }

    @Test
    fun searchQuery_cleared_returnsFullList() = runTest(testDispatcher) {
        val totoro = makeFilm("1", "My Neighbor Totoro")
        val spirited = makeFilm("2", "Spirited Away")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(totoro, spirited))

        every { mockRepository.getAllFavourites() } returns favouritesFlow

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.SearchQueryChanged("Spirited"))
        advanceUntilIdle()
        assertEquals(1, (vm.uiState.value as FavouritesUiState.Success).films.size)

        vm.onEvent(FavouritesEvent.SearchQueryChanged(""))
        advanceUntilIdle()
        assertEquals(2, (vm.uiState.value as FavouritesUiState.Success).films.size)

        stateJob.cancel()
    }

    @Test
    fun daoUpdate_whileSearchActive_filtersNewResults() = runTest(testDispatcher) {
        val totoro = makeFilm("1", "My Neighbor Totoro")
        val spirited = makeFilm("2", "Spirited Away")
        val favouritesFlow = MutableSharedFlow<List<Film>>(replay = 1)
        favouritesFlow.emit(listOf(totoro))

        every { mockRepository.getAllFavourites() } returns favouritesFlow

        val vm = FavouritesViewModel(mockRepository)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // Поиск "Spirited" — нет совпадений
        vm.onEvent(FavouritesEvent.SearchQueryChanged("Spirited"))
        advanceUntilIdle()
        assertEquals(FavouritesUiState.Empty, vm.uiState.value)

        // DAO добавляет новый фильм — combine реагирует автоматически
        favouritesFlow.emit(listOf(totoro, spirited))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is FavouritesUiState.Success)
        assertEquals("Spirited Away", (state as FavouritesUiState.Success).films[0].title)

        stateJob.cancel()
    }
}
