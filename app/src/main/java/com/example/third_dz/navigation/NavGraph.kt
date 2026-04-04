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
import com.example.third_dz.ui.viewmodel.FilmDetailViewModel
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.ui.viewmodel.FavouritesViewModel

sealed class Screen(val route: String) {
    data object List : Screen("list")
    data object Favourites : Screen("favourites")
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
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                onEvent = viewModel::onEvent,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                },
                onFavouritesClick = {
                    navController.navigate(Screen.Favourites.route)
                }
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
            FilmDetailScreen(
                filmId = filmId,
                state = viewModel.uiState,
                onEvent = viewModel::onEvent,
                onLoadFilm = viewModel::loadFilm,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
