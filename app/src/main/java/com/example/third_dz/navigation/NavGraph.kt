package com.example.third_dz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.third_dz.ui.screen.FilmDetailScreen
import com.example.third_dz.ui.screen.FilmsListScreen
import com.example.third_dz.ui.screen.FavouritesScreen
import com.example.third_dz.ui.screen.collections.CollectionDetailScreen
import com.example.third_dz.ui.screen.collections.CollectionsListScreen
import com.example.third_dz.ui.viewmodel.CollectionDetailViewModel
import com.example.third_dz.ui.viewmodel.CollectionsListViewModel
import com.example.third_dz.ui.viewmodel.FilmDetailViewModel
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.ui.viewmodel.FavouritesViewModel

sealed class Screen(val route: String) {
    data object List : Screen("list")
    data object Favourites : Screen("favourites")
    data object Collections : Screen("collections")
    data class CollectionDetail(val collectionId: Long = 0L) : Screen("collection_detail/{collectionId}") {
        fun createRoute(collectionId: Long) = "collection_detail/$collectionId"
    }
    data class Detail(val filmId: String = "{filmId}") : Screen("detail/{filmId}") {
        fun createRoute(filmId: String) = "detail/$filmId"
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            val viewModel: FilmsListViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                statusFilter = statusFilter,
                onEvent = viewModel::onEvent,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                },
                onFavouritesClick = {
                    navController.navigate(Screen.Favourites.route)
                }
            )
        }

        composable(Screen.Collections.route) {
            val viewModel: CollectionsListViewModel = hiltViewModel()
            val collections by viewModel.collections.collectAsStateWithLifecycle()
            CollectionsListScreen(
                collections = collections,
                onCreate = viewModel::create,
                onDelete = viewModel::delete,
                onCollectionClick = { id ->
                    navController.navigate(Screen.CollectionDetail().createRoute(id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CollectionDetail().route,
            arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
        ) {
            val viewModel: CollectionDetailViewModel = hiltViewModel()
            val collection by viewModel.collection.collectAsStateWithLifecycle()
            val films by viewModel.films.collectAsStateWithLifecycle()
            CollectionDetailScreen(
                collection = collection,
                films = films,
                onRename = viewModel::rename,
                onDelete = {
                    viewModel.delete()
                    navController.popBackStack()
                },
                onRemoveFilm = viewModel::removeFilm,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Favourites.route) {
            val viewModel: FavouritesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
            FavouritesScreen(
                state = state,
                searchQuery = searchQuery,
                sortOrder = sortOrder,
                filmRemovedEvent = viewModel.filmRemovedEvent,
                onEvent = viewModel::onEvent,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Detail().route,
            arguments = listOf(
                navArgument("filmId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val filmId = backStackEntry.arguments?.getString("filmId") ?: return@composable
            val viewModel: FilmDetailViewModel = hiltViewModel()
            val collections by viewModel.collections.collectAsStateWithLifecycle()
            val memberIds by viewModel.memberIds.collectAsStateWithLifecycle()
            FilmDetailScreen(
                filmId = filmId,
                state = viewModel.uiState,
                collections = collections,
                memberIds = memberIds,
                onToggleCollection = { id, member -> viewModel.toggleCollection(filmId, id, member) },
                onEvent = viewModel::onEvent,
                onLoadFilm = viewModel::loadFilm,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
