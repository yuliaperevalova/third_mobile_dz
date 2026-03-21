package com.example.third_dz.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.model.Film
import com.example.third_dz.ui.event.FilmsListEvent
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

    // Test 4 (нетривиальный UI): Error state → клик Retry → Success state с фильмом
    @Test
    fun errorState_retryButton_clickLeadsToSuccess() {
        val film = makeFilm()
        var state by mutableStateOf<FilmsListUiState>(FilmsListUiState.Error("network error"))

        composeRule.setContent {
            FilmsListScreen(
                state = state,
                onEvent = { event ->
                    if (event is FilmsListEvent.Refresh) {
                        state = FilmsListUiState.Success(listOf(film), emptySet())
                    }
                },
                onFilmClick = {},
                onFavouritesClick = {}
            )
        }

        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithText(film.title).assertIsDisplayed()
    }
}
