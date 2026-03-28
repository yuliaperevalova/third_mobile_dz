package com.example.third_dz.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.model.Film
import com.example.third_dz.ui.state.FilmsListUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilmsListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

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

    @Test
    fun errorState_retryButtonIsDisplayed() {
        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Error("network error"),
                onEvent = {},
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun successState_filmTitleIsDisplayed() {
        composeRule.setContent {
            FilmsListScreen(
                state = FilmsListUiState.Success(listOf(makeFilm()), emptySet()),
                onEvent = {},
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.onNodeWithText("Spirited Away").assertIsDisplayed()
    }
}
