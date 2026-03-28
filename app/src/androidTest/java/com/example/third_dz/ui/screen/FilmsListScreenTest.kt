package com.example.third_dz.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilmsListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val mockRepository = mockk<GhibliFilmsRepository>()
    private val mockDao = mockk<FavouriteFilmDao>()

    private fun makeFilm(id: String = "1") = Film(
        id = id,
        title = "Spirited Away",
        original_title = "千と千尋の神隠し",
        original_title_romanised = "Sen to Chihiro no Kamikakushi",
        description = "A girl enters a spirit world",
        director = "Hayao Miyazaki",
        producer = "Toshio Suzuki",
        release_date = "2001",
        running_time = "125",
        rt_score = "97",
        people = emptyList(),
        species = emptyList(),
        locations = emptyList(),
        vehicles = emptyList(),
        url = "https://ghibliapi.vercel.app/films/$id"
    )

    private fun createViewModel(): FilmsListViewModel {
        return ViewModelProvider(
            composeRule.activity,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FilmsListViewModel(mockRepository, mockDao) as T
            }
        )[FilmsListViewModel::class.java]
    }

    @Test
    fun errorState_retryButtonIsDisplayed() {
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } throws RuntimeException("network error")

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            FilmsListScreen(
                state = state,
                onEvent = vm::onEvent,
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_filmTitleIsDisplayed() {
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } returns listOf(makeFilm())

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            FilmsListScreen(
                state = state,
                onEvent = vm::onEvent,
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText("Spirited Away").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Spirited Away").assertIsDisplayed()
    }

    // Контракт retry на уровне приложения: ViewModel получает событие, вызывает репозиторий,
    // состояние меняется — экран отображает результат
    @Test
    fun retryButton_click_triggersNewLoadAndShowsFilm() {
        val film = makeFilm()
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } throws RuntimeException("network error")

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            FilmsListScreen(
                state = state,
                onEvent = vm::onEvent,
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty()
        }

        coEvery { mockRepository.getAllFilms(any()) } returns listOf(film)
        composeRule.onNodeWithText("Retry").performClick()

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText(film.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(film.title).assertIsDisplayed()
    }
}
