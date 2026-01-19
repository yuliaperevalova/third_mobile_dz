package com.example.third_dz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    listViewModel: FilmsListViewModel,
    detailViewModel: FilmDetailViewModel,
    favouritesViewModel: FavouritesViewModel
) {
    val listState by listViewModel.uiState.collectAsStateWithLifecycle()
    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val favouritesState by favouritesViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            FilmsListScreen(
                state = listState,
                onEvent = { event -> listViewModel.onEvent(event) },
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                },
                onFavouritesClick = {
                    navController.navigate(Screen.Favourites.route)
                }
            )
        }
        
        composable(Screen.Favourites.route) {
            FavouritesScreen(
                state = favouritesState,
                onEvent = { event -> favouritesViewModel.onEvent(event) },
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
                androidx.navigation.navArgument("filmId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val filmId = backStackEntry.arguments?.getString("filmId") ?: return@composable
            FilmDetailScreen(
                filmId = filmId,
                state = detailState,
                onEvent = { event -> detailViewModel.onEvent(event) },
                onLoadFilm = { id -> detailViewModel.loadFilm(id) },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

