package com.example.third_dz.ui.viewmodel.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.model.Vehicle
import com.example.third_dz.data.repository.UniverseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VehiclesListViewModel @Inject constructor(
    repository: UniverseRepository
) : ViewModel() {

    val vehicles: StateFlow<List<Vehicle>> = repository.observeAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
