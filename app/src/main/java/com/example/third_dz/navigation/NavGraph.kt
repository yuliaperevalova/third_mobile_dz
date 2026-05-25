package com.example.third_dz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.third_dz.ui.screen.collections.CollectionDetailScreen
import com.example.third_dz.ui.screen.collections.CollectionsListScreen
import com.example.third_dz.ui.screen.history.HistoryScreen
import com.example.third_dz.ui.screen.locations.LocationDetailScreen
import com.example.third_dz.ui.screen.people.PersonDetailScreen
import com.example.third_dz.ui.screen.settings.SettingsScreen
import com.example.third_dz.ui.screen.universe.UniverseTabsScreen
import com.example.third_dz.ui.viewmodel.CollectionDetailViewModel
import com.example.third_dz.ui.viewmodel.CollectionsListViewModel
import com.example.third_dz.ui.viewmodel.FilmDetailViewModel
import com.example.third_dz.ui.viewmodel.FilmsListViewModel
import com.example.third_dz.ui.viewmodel.HistoryViewModel
import com.example.third_dz.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    data object List : Screen("list")
    data object Collections : Screen("collections")
    data class CollectionDetail(val collectionId: Long = 0L) : Screen("collection_detail/{collectionId}") {
        fun createRoute(collectionId: Long) = "collection_detail/$collectionId"
    }
    data class Detail(val filmId: String = "{filmId}") : Screen("detail/{filmId}") {
        fun createRoute(filmId: String) = "detail/$filmId"
    }
    data object Universe : Screen("universe")
    data class PersonDetail(val personId: String = "{personId}") : Screen("person_detail/{personId}") {
        fun createRoute(personId: String) = "person_detail/$personId"
    }
    data class LocationDetail(val locationId: String = "{locationId}") : Screen("location_detail/{locationId}") {
        fun createRoute(locationId: String) = "location_detail/$locationId"
    }
    data object Settings : Screen("settings")
    data object History : Screen("history")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route,
        modifier = modifier
    ) {
        composable(Screen.List.route) {
            val viewModel: FilmsListViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
            val recentItems by viewModel.recentItems.collectAsStateWithLifecycle()
            FilmsListScreen(
                state = state,
                searchQuery = searchQuery,
                statusFilter = statusFilter,
                recentItems = recentItems,
                onEvent = viewModel::onEvent,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                },
                onCollectionsClick = {
                    navController.navigate(Screen.Collections.route)
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

        composable(Screen.Universe.route) {
            UniverseTabsScreen(
                onPersonClick = { personId ->
                    navController.navigate(Screen.PersonDetail().createRoute(personId))
                },
                onLocationClick = { locationId ->
                    navController.navigate(Screen.LocationDetail().createRoute(locationId))
                }
            )
        }

        composable(
            route = Screen.PersonDetail().route,
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId") ?: return@composable
            val viewModel: com.example.third_dz.ui.viewmodel.people.PersonDetailViewModel = hiltViewModel()
            val person by viewModel.loadPerson(personId).collectAsStateWithLifecycle()
            PersonDetailScreen(
                person = person,
                isPinned = false,
                onTogglePin = viewModel::onTogglePin,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                }
            )
        }

        composable(
            route = Screen.LocationDetail().route,
            arguments = listOf(navArgument("locationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            val viewModel: com.example.third_dz.ui.viewmodel.locations.LocationDetailViewModel = hiltViewModel()
            val location by viewModel.loadLocation(locationId).collectAsStateWithLifecycle()
            LocationDetailScreen(
                location = location,
                isPinned = false,
                onTogglePin = viewModel::onTogglePin,
                onFilmClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val cacheTtlMinutes by viewModel.cacheTtlMinutes.collectAsStateWithLifecycle()
            val wifiOnlyRefresh by viewModel.wifiOnlyRefresh.collectAsStateWithLifecycle()
            val refreshIntervalHours by viewModel.refreshIntervalHours.collectAsStateWithLifecycle()
            val defaultListSort by viewModel.defaultListSort.collectAsStateWithLifecycle()
            val historyLimit by viewModel.historyLimit.collectAsStateWithLifecycle()
            SettingsScreen(
                themeMode = themeMode,
                cacheTtlMinutes = cacheTtlMinutes,
                wifiOnlyRefresh = wifiOnlyRefresh,
                refreshIntervalHours = refreshIntervalHours,
                defaultListSort = defaultListSort,
                historyLimit = historyLimit,
                onThemeModeChange = viewModel::setThemeMode,
                onCacheTtlMinutesChange = viewModel::setCacheTtlMinutes,
                onWifiOnlyRefreshChange = viewModel::setWifiOnlyRefresh,
                onRefreshIntervalHoursChange = viewModel::setRefreshIntervalHours,
                onDefaultListSortChange = viewModel::setDefaultListSort,
                onHistoryLimitChange = viewModel::setHistoryLimit,
                onClearAllUserData = viewModel::clearAllUserData,
                onRefreshUniverse = viewModel::refreshUniverseNow,
                onExportBackup = viewModel::exportBackup,
                onImportBackup = viewModel::importBackup,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            HistoryScreen(
                state = state,
                onClearAll = viewModel::clearAll,
                onDeleteItem = viewModel::deleteItem,
                onItemClick = { filmId ->
                    navController.navigate(Screen.Detail(filmId).createRoute(filmId))
                }
            )
        }
    }
}
