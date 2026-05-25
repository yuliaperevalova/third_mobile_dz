package com.example.third_dz.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.ui.state.FilmsListUiState
import com.example.third_dz.util.makeFilm
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilmsListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun errorState_retryButtonIsDisplayed() {
        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Error("Network error"),
                searchQuery = "",
                statusFilter = null,
                recentItems = emptyList(),
                onEvent = {},
                onFilmClick = {},
                onCollectionsClick = {}
            )
        }

        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_filmTitleIsDisplayed() {
        val film = makeFilm()
        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(listOf(film), emptyMap()),
                searchQuery = "",
                statusFilter = null,
                recentItems = emptyList(),
                onEvent = {},
                onFilmClick = {},
                onCollectionsClick = {}
            )
        }

        composeRule.onNodeWithText(film.title).assertIsDisplayed()
    }

    @Test
    fun filmClick_invokesOnFilmClickWithCorrectFilmId() {
        val film = makeFilm("1")
        var clickedFilmId: String? = null

        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(listOf(film), emptyMap()),
                searchQuery = "",
                statusFilter = null,
                recentItems = emptyList(),
                onEvent = {},
                onFilmClick = { clickedFilmId = it },
                onCollectionsClick = {}
            )
        }

        composeRule.onNodeWithText(film.title).performClick()
        assert(clickedFilmId == "1") { "Expected filmId '1', got '$clickedFilmId'" }
    }

    @Test
    fun collectionsButton_click_invokesOnCollectionsClick() {
        var collectionsClicked = false

        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(emptyList(), emptyMap()),
                searchQuery = "",
                statusFilter = null,
                recentItems = emptyList(),
                onEvent = {},
                onFilmClick = {},
                onCollectionsClick = { collectionsClicked = true }
            )
        }

        composeRule.onNodeWithContentDescription("Collections").performClick()
        assert(collectionsClicked) { "Expected collectionsClick to be invoked" }
    }

    @Test
    fun emptyState_showsMessage() {
        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Empty,
                searchQuery = "",
                statusFilter = null,
                recentItems = emptyList(),
                onEvent = {},
                onFilmClick = {},
                onCollectionsClick = {}
            )
        }

        composeRule.onNodeWithText("No films found").assertIsDisplayed()
    }
}
