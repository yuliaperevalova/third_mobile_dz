package com.example.third_dz.ui.screen.universe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.third_dz.ui.screen.locations.LocationsListScreen
import com.example.third_dz.ui.screen.people.PeopleListScreen
import com.example.third_dz.ui.screen.species.SpeciesListScreen
import com.example.third_dz.ui.screen.vehicles.VehiclesListScreen
import com.example.third_dz.ui.viewmodel.locations.LocationsListViewModel
import com.example.third_dz.ui.viewmodel.people.PeopleListViewModel
import com.example.third_dz.ui.viewmodel.species.SpeciesListViewModel
import com.example.third_dz.ui.viewmodel.vehicles.VehiclesListViewModel

@Composable
fun UniverseTabsScreen(
    onPersonClick: (String) -> Unit,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val peopleVm: PeopleListViewModel = hiltViewModel()
    val locationsVm: LocationsListViewModel = hiltViewModel()
    val speciesVm: SpeciesListViewModel = hiltViewModel()
    val vehiclesVm: VehiclesListViewModel = hiltViewModel()

    val people by peopleVm.people.collectAsStateWithLifecycle()
    val locations by locationsVm.locations.collectAsStateWithLifecycle()
    val species by speciesVm.species.collectAsStateWithLifecycle()
    val vehicles by vehiclesVm.vehicles.collectAsStateWithLifecycle()

    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("People", "Locations", "Species", "Vehicles")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (tabIndex) {
            0 -> PeopleListScreen(people = people, onPersonClick = onPersonClick, modifier = Modifier.padding(8.dp))
            1 -> LocationsListScreen(locations = locations, onLocationClick = onLocationClick, modifier = Modifier.padding(8.dp))
            2 -> SpeciesListScreen(species = species, modifier = Modifier.padding(8.dp))
            3 -> VehiclesListScreen(vehicles = vehicles, modifier = Modifier.padding(8.dp))
        }
    }
}
