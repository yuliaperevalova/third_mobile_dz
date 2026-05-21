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
import com.example.third_dz.data.repository.FavouritesRepository
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.usecase.collection.AddFilmToCollectionUseCase
import com.example.third_dz.domain.usecase.collection.ObserveCollectionsForFilmUseCase
import com.example.third_dz.domain.usecase.collection.ObserveCollectionsUseCase
import com.example.third_dz.domain.usecase.collection.RemoveFilmFromCollectionUseCase
import com.example.third_dz.domain.usecase.record.ObserveFilmRecordUseCase
import com.example.third_dz.domain.usecase.record.SetNoteUseCase
import com.example.third_dz.domain.usecase.record.SetRatingUseCase
import com.example.third_dz.domain.usecase.record.SetWatchStatusUseCase
import com.example.third_dz.ui.screen.FavouritesScreen
import com.example.third_dz.ui.screen.FilmDetailScreen
import com.example.third_dz.ui.screen.FilmsListScreen
import com.example.third_dz.ui.viewmodel.FavouritesViewModel
import com.example.third_dz.ui.viewmodel.FilmDetailViewModel
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.util.makeFilm
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
    private val mockRecordRepository = mockk<UserFilmRecordRepository>(relaxed = true)
    private val mockFavouritesRepository = mockk<FavouritesRepository>()
    private val mockSetStatus = mockk<SetWatchStatusUseCase>(relaxed = true)
    private val mockSetRating = mockk<SetRatingUseCase>(relaxed = true)
    private val mockSetNote = mockk<SetNoteUseCase>(relaxed = true)
    private val mockObserveRecord = mockk<ObserveFilmRecordUseCase>().also {
        every { it.invoke(any()) } returns flowOf(null)
    }
    private val mockObserveCollections = mockk<ObserveCollectionsUseCase>().also {
        every { it.invoke() } returns flowOf(emptyList())
    }
    private val mockObserveCollectionsForFilm = mockk<ObserveCollectionsForFilmUseCase>().also {
        every { it.invoke(any()) } returns flowOf(emptyList())
    }
    private val mockAddFilmToCollection = mockk<AddFilmToCollectionUseCase>(relaxed = true)
    private val mockRemoveFilmFromCollection = mockk<RemoveFilmFromCollectionUseCase>(relaxed = true)

    @Composable
    private fun TestNavGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "list") {
            composable("list") { entry ->
                val factory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            FilmsListViewModel(mockRepository, mockRecordRepository) as T
                    }
                }
                val vm = ViewModelProvider(entry, factory)[FilmsListViewModel::class.java]
                val state by vm.uiState.collectAsState()
                val searchQuery by vm.searchQuery.collectAsState()
                val statusFilter by vm.statusFilter.collectAsState()
                FilmsListScreen(
                    state = state,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
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
                val sortOrder by vm.sortOrder.collectAsState()
                FavouritesScreen(
                    state = state,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
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
                            FilmDetailViewModel(
                                mockRepository,
                                mockSetStatus,
                                mockSetRating,
                                mockSetNote,
                                mockObserveRecord,
                                mockObserveCollections,
                                mockObserveCollectionsForFilm,
                                mockAddFilmToCollection,
                                mockRemoveFilmFromCollection
                            ) as T
                    }
                }
                val vm = ViewModelProvider(entry, factory)[FilmDetailViewModel::class.java]
                FilmDetailScreen(
                    filmId = filmId,
                    state = vm.uiState,
                    collections = emptyList(),
                    memberIds = emptySet(),
                    onToggleCollection = { _, _ -> },
                    onEvent = vm::onEvent,
                    onLoadFilm = vm::loadFilm,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    @Test
    fun filmCardClick_navigatesToDetailScreen() {
        val film = makeFilm("42")
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns flowOf(listOf(film))
        coEvery { mockRepository.refreshFilms() } returns Unit
        coEvery { mockRepository.getFilmById("42") } returns film

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

    @Test
    fun favouritesIconClick_navigatesToFavouritesScreen() {
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit
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

    @Test
    fun backButtonFromFavourites_popsBackToList() {
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns flowOf(emptyList())
        coEvery { mockRepository.refreshFilms() } returns Unit
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

    @Test
    fun backButtonFromDetail_popsBackToList() {
        val film = makeFilm("7")
        every { mockRecordRepository.observeAll() } returns flowOf(emptyList())
        every { mockRepository.getFilmsFlow() } returns flowOf(listOf(film))
        coEvery { mockRepository.refreshFilms() } returns Unit
        coEvery { mockRepository.getFilmById("7") } returns film

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
