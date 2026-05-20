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
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
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
    private val mockRecordRepository = mockk<UserFilmRecordRepository>()

    private fun createViewModel(): FilmsListViewModel {
        return ViewModelProvider(
            composeRule.activity,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FilmsListViewModel(mockRepository, mockRecordRepository) as T
            }
        )[FilmsListViewModel::class.java]
    }

    @Test
    fun errorState_retryButtonIsDisplayed() {
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            val searchQuery by vm.searchQuery.collectAsState()
            val statusFilter by vm.statusFilter.collectAsState()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                statusFilter = statusFilter,
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
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns flowOf(listOf(makeFilm()))
        coEvery { mockRepository.refreshFilms() } returns Unit

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            val searchQuery by vm.searchQuery.collectAsState()
            val statusFilter by vm.statusFilter.collectAsState()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                statusFilter = statusFilter,
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

    @Test
    fun retryButton_click_triggersNewLoadAndShowsFilm() {
        val film = makeFilm()
        val filmsFlow = MutableStateFlow<List<Film>>(emptyList())
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns filmsFlow
        coEvery { mockRepository.refreshFilms() } throws RuntimeException("network error")

        val vm = createViewModel()
        composeRule.setContent {
            val state by vm.uiState.collectAsState()
            val searchQuery by vm.searchQuery.collectAsState()
            val statusFilter by vm.statusFilter.collectAsState()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                statusFilter = statusFilter,
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

    @Test
    fun filmClick_invokesOnFilmClickWithCorrectFilmId() {
        val film = makeFilm("1")
        val onFilmClick = mockk<(String) -> Unit>(relaxed = true)

        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(listOf(film), emptyMap<String, UserFilmRecord>()),
                searchQuery = "",
                statusFilter = null,
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

    @Test
    fun favouritesButton_click_invokesOnFavouritesClick() {
        val onFavouritesClick = mockk<() -> Unit>(relaxed = true)

        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(emptyList(), emptyMap<String, UserFilmRecord>()),
                searchQuery = "",
                statusFilter = null,
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
