package com.example.third_dz.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.repository.FavouritesRepository
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.util.makeFilm
import com.example.third_dz.ui.screen.FavouritesScreen
import com.example.third_dz.ui.screen.FilmDetailScreen
import com.example.third_dz.ui.screen.FilmsListScreen
import com.example.third_dz.ui.viewmodel.FavouritesViewModel
import com.example.third_dz.ui.viewmodel.FilmDetailViewModel
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphIntegrationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val mockRepository = mockk<GhibliFilmsRepository>()
    private val mockDao = mockk<FavouriteFilmDao>()
    private val mockFavouritesRepository = mockk<FavouritesRepository>()

    // Зеркало NavGraph.kt с ручными фабриками вместо hiltViewModel() —
    // позволяет монтировать полный граф навигации без Hilt в тестах
    @Composable
    private fun TestNavGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "list") {
            composable("list") { entry ->
                val factory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            FilmsListViewModel(mockRepository, mockDao) as T
                    }
                }
                val vm = ViewModelProvider(entry, factory)[FilmsListViewModel::class.java]
                val state by vm.uiState.collectAsState()
                FilmsListScreen(
                    state = state,
                    onEvent = vm::onEvent,
                    onFilmClick = { filmId -> navController.navigate("detail/$filmId") },
                    onFavouritesClick = { navController.navigate("favourites") }
                )
            }
            composable("favourites") { entry ->
                val factory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            FavouritesViewModel(mockFavouritesRepository) as T
                    }
                }
                val vm = ViewModelProvider(entry, factory)[FavouritesViewModel::class.java]
                val state by vm.uiState.collectAsState()
                val searchQuery by vm.searchQuery.collectAsState()
                FavouritesScreen(
                    state = state,
                    searchQuery = searchQuery,
                    filmRemovedEvent = vm.filmRemovedEvent,
                    onEvent = vm::onEvent,
                    onFilmClick = { filmId -> navController.navigate("detail/$filmId") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "detail/{filmId}",
                arguments = listOf(navArgument("filmId") { type = NavType.StringType })
            ) { entry ->
                val filmId = entry.arguments?.getString("filmId") ?: return@composable
                val factory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            FilmDetailViewModel(mockRepository, mockDao) as T
                    }
                }
                val vm = ViewModelProvider(entry, factory)[FilmDetailViewModel::class.java]
                FilmDetailScreen(
                    filmId = filmId,
                    state = vm.uiState,
                    onEvent = vm::onEvent,
                    onLoadFilm = vm::loadFilm,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    // Контракт навигации: клик по карточке фильма → экран деталей с правильным filmId
    @Test
    fun filmCardClick_navigatesToDetailScreen() {
        val film = makeFilm("42")
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } returns listOf(film)
        coEvery { mockRepository.getFilmById("42") } returns film
        coEvery { mockDao.isFavourite("42") } returns false

        var navController: NavHostController? = null
        composeRule.setContent {
            navController = rememberNavController()
            TestNavGraph(navController = navController!!)
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText(film.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(film.title).performClick()

        composeRule.runOnIdle {
            assertEquals("detail/{filmId}", navController?.currentDestination?.route)
            assertEquals("42", navController?.currentBackStackEntry?.arguments?.getString("filmId"))
        }
    }

    // Контракт навигации: клик по иконке избранного → экран Favourites
    @Test
    fun favouritesIconClick_navigatesToFavouritesScreen() {
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } returns emptyList()
        every { mockFavouritesRepository.getAllFavourites() } returns flowOf(emptyList())

        var navController: NavHostController? = null
        composeRule.setContent {
            navController = rememberNavController()
            TestNavGraph(navController = navController!!)
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithContentDescription("Favourites").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Favourites").performClick()

        composeRule.runOnIdle {
            assertEquals("favourites", navController?.currentDestination?.route)
        }
    }

    // Контракт навигации: Back из Favourites → возврат на список
    @Test
    fun backButtonFromFavourites_popsBackToList() {
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } returns emptyList()
        every { mockFavouritesRepository.getAllFavourites() } returns flowOf(emptyList())

        var navController: NavHostController? = null
        composeRule.setContent {
            navController = rememberNavController()
            TestNavGraph(navController = navController!!)
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithContentDescription("Favourites").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Favourites").performClick()

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.runOnIdle {
            assertEquals("list", navController?.currentDestination?.route)
        }
    }

    // Контракт навигации: Back из Detail → возврат на список
    @Test
    fun backButtonFromDetail_popsBackToList() {
        val film = makeFilm("7")
        every { mockDao.getAllFavourites() } returns flowOf(emptyList())
        coEvery { mockRepository.getAllFilms(any()) } returns listOf(film)
        coEvery { mockRepository.getFilmById("7") } returns film
        coEvery { mockDao.isFavourite("7") } returns false

        var navController: NavHostController? = null
        composeRule.setContent {
            navController = rememberNavController()
            TestNavGraph(navController = navController!!)
        }

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText(film.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(film.title).performClick()

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.runOnIdle {
            assertEquals("list", navController?.currentDestination?.route)
        }
    }
}
