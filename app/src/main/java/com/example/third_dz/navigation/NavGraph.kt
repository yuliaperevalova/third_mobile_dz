package com.example.third_dz.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.third_dz.ui.screen.FactDetailScreen
import com.example.third_dz.ui.screen.FactsListScreen
import com.example.third_dz.ui.screen.FavouritesScreen
import com.example.third_dz.ui.viewmodel.FactDetailViewModel
import com.example.third_dz.ui.viewmodel.FactsListViewModel
import com.example.third_dz.ui.viewmodel.FavouritesViewModel

sealed class Screen(val route: String) {
    data object List : Screen("list")
    data object Favourites : Screen("favourites")
    data class Detail(val factId: String = "{factId}") : Screen("detail/{factId}") {
        fun createRoute(factId: String) = "detail/$factId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    listViewModel: FactsListViewModel,
    detailViewModel: FactDetailViewModel,
    favouritesViewModel: FavouritesViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            FactsListScreen(
                viewModel = listViewModel,
                onFactClick = { factId ->
                    navController.navigate(Screen.Detail(factId).createRoute(factId))
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
                onFactClick = { factId ->
                    navController.navigate(Screen.Detail(factId).createRoute(factId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Detail().route,
            arguments = listOf(
                androidx.navigation.navArgument("factId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val factId = backStackEntry.arguments?.getString("factId") ?: return@composable
            FactDetailScreen(
                factId = factId,
                detailViewModel = detailViewModel,
                listViewModel = listViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

