package com.example.third_dz.ui.viewmodel

import app.cash.turbine.test
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
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
    private val mockDao = mockk<FavouriteFilmDao>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeFavouriteEntity(id: String = "1") = FavouriteFilmEntity(
        id = id,
        title = "My Neighbor Totoro",
        original_title = "となりのトトロ",
        original_title_romanised = "Tonari no Totoro",
        description = "Two sisters move to the country",
        director = "Hayao Miyazaki",
        producer = "Toshio Suzuki",
        release_date = "1988",
        running_time = "86",
        rt_score = "93",
        url = "https://ghibliapi.vercel.app/films/$id"
    )

    // Test 6: пустая БД при запуске → состояние Empty
    @Test
    fun initialState_withEmptyDb_emitsEmpty() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())

        val vm = FavouritesViewModel(mockDao)

        val collected = mutableListOf<FavouritesUiState>()
        val job = launch { vm.uiState.collect { collected.add(it) } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(collected.contains(FavouritesUiState.Empty))
    }

    // Test Flow-3 (нетривиальный): flatMapLatest отменяет устаревший запрос —
    // результат первого (медленного) flow не попадает в uiState после retry
    @Test
    fun retry_cancelsStaleFlow_staleResultNeverReachesUiState() = runTest(testDispatcher) {
        // Первый канал — "медленный": буферизированный, send() не блокируется даже без получателя
        val staleChannel = Channel<List<FavouriteFilmEntity>>(Channel.BUFFERED)
        // Второй канал — сразу эмитит пустой список
        val freshChannel = Channel<List<FavouriteFilmEntity>>(Channel.UNLIMITED)
        freshChannel.send(emptyList())

        var callCount = 0
        every { mockDao.getAllFavourites() } answers {
            callCount++
            if (callCount == 1) staleChannel.receiveAsFlow()
            else freshChannel.receiveAsFlow()
        }

        val vm = FavouritesViewModel(mockDao)

        vm.uiState.test {
            assertEquals(FavouritesUiState.Loading, awaitItem())

            // Запускаем корутины: первая подписка на staleChannel (ещё ничего не эмитит)
            testDispatcher.scheduler.advanceUntilIdle()

            // Retry — flatMapLatest отменяет staleChannel и переключается на freshChannel
            vm.onEvent(FavouritesEvent.Retry)
            testDispatcher.scheduler.advanceUntilIdle()

            // freshChannel сразу отдал пустой список → Empty
            assertEquals(FavouritesUiState.Empty, awaitItem())

            // Теперь "запаздывает" результат из staleChannel — отправляем данные в него
            val staleEntities = listOf(makeFavouriteEntity("stale"))
            staleChannel.send(staleEntities)
            testDispatcher.scheduler.advanceUntilIdle()

            // Устаревший результат не должен попасть в uiState — канал уже отменён
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test Flow-4: удаление фильма обновляет uiState И эмитит одноразовое событие filmRemovedEvent
    @Test
    fun toggleFavourite_updatesStateAndEmitsRemovedEvent() = runTest(testDispatcher) {
        val entity = makeFavouriteEntity("1")
        val favouritesFlow = MutableSharedFlow<List<FavouriteFilmEntity>>(replay = 1)
        favouritesFlow.emit(listOf(entity))

        every { mockDao.getAllFavourites() } returns favouritesFlow
        coEvery { mockDao.delete(any()) } returns Unit

        val vm = FavouritesViewModel(mockDao)

        // WhileSubscribed требует активного подписчика, иначе upstream не работает
        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.filmRemovedEvent.test {
            vm.onEvent(FavouritesEvent.ToggleFavourite("1"))
            // Симулируем что DAO обновил базу — список стал пустым
            favouritesFlow.emit(emptyList())
            advanceUntilIdle()

            // uiState обновился — фильм пропал
            assertTrue(vm.uiState.value is FavouritesUiState.Empty)

            // событие сработало ровно один раз с нужным id
            assertEquals("1", awaitItem())
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }

        stateJob.cancel()
    }

    // Test Flow-5: новый подписчик filmRemovedEvent не получает старое событие (replay = 0)
    @Test
    fun filmRemovedEvent_newSubscriberDoesNotReceivePastEvent() = runTest(testDispatcher) {
        val entity = makeFavouriteEntity("1")
        val favouritesFlow = MutableSharedFlow<List<FavouriteFilmEntity>>(replay = 1)
        favouritesFlow.emit(listOf(entity))

        every { mockDao.getAllFavourites() } returns favouritesFlow
        coEvery { mockDao.delete(any()) } returns Unit

        val vm = FavouritesViewModel(mockDao)

        val stateJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // Удаляем фильм — событие эмитируется
        vm.onEvent(FavouritesEvent.ToggleFavourite("1"))
        advanceUntilIdle()

        stateJob.cancel()

        // Новый подписчик подключается ПОСЛЕ события — не должен ничего получить
        vm.filmRemovedEvent.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test 7 (нетривиальный Flow): emit в retryTrigger действительно создаёт новую подписку
    // через flatMapLatest — проверяем контракт, а не только финальный state.value
    @Test
    fun retry_triggersNewFlatMapLatestSubscription() = runTest(testDispatcher) {
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())

        val vm = FavouritesViewModel(mockDao)

        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onEvent(FavouritesEvent.Retry)
        advanceUntilIdle()

        job.cancel()

        // flatMapLatest при каждом новом значении retryTrigger отменяет старый поток
        // и создаёт новую подписку — getAllFavourites должен быть вызван ровно дважды
        verify(exactly = 2) { mockDao.getAllFavourites() }
    }
}
