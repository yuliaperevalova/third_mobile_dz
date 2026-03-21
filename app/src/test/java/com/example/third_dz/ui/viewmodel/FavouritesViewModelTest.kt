package com.example.third_dz.ui.viewmodel

import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
import com.example.third_dz.ui.event.FavouritesEvent
import com.example.third_dz.ui.state.FavouritesUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
