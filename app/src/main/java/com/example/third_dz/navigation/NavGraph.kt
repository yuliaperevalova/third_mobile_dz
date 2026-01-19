package com.example.third_dz.navigation

import androidx.compose.runtime.Composable
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
    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            FilmsListScreen(
                viewModel = listViewModel,
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
                listViewModel = listViewModel,
                favouritesViewModel = favouritesViewModel,
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
                detailViewModel = detailViewModel,
                listViewModel = listViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

