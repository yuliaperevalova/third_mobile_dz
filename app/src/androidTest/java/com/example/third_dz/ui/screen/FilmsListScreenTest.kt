package com.example.third_dz.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.state.FilmsListUiState
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.util.makeFilm
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            val searchQuery by vm.searchQuery.collectAsState()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
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
        every { mockRepository.getFilmsFlow() } returns flowOf(listOf(makeFilm()))
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            val searchQuery by vm.searchQuery.collectAsState()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
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
        val filmsFlow = MutableStateFlow<List<Film>>(emptyList())
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns filmsFlow
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            val searchQuery by vm.searchQuery.collectAsState()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                onEvent = vm::onEvent,
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty()
        }

        coEvery { mockRepository.refreshFilms() } answers { filmsFlow.value = listOf(film) }
        composeRule.onNodeWithText("Retry").performClick()

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText(film.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(film.title).assertIsDisplayed()
    }

    // Контракт callback: клик по карточке фильма передаёт правильный filmId в onFilmClick
    @Test
    fun filmClick_invokesOnFilmClickWithCorrectFilmId() {
        val film = makeFilm("1")
        val onFilmClick = mockk<(String) -> Unit>(relaxed = true)

        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(listOf(film), emptySet()),
                searchQuery = "",
                onEvent = {},
                onFilmClick = onFilmClick,
                onFavouritesClick = {}
            )
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText(film.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(film.title).performClick()

        verify { onFilmClick("1") }
    }

    // Контракт callback: клик по иконке избранного вызывает onFavouritesClick
    @Test
    fun favouritesButton_click_invokesOnFavouritesClick() {
        val onFavouritesClick = mockk<() -> Unit>(relaxed = true)

        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(emptyList(), emptySet()),
                searchQuery = "",
                onEvent = {},
                onFilmClick = {},
                onFavouritesClick = onFavouritesClick
            )
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithContentDescription("Favourites").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Favourites").performClick()

        verify { onFavouritesClick() }
    }
}
