package com.example.third_dz.ui.viewmodel.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Vehicle
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.domain.usecase.pin.TogglePinUseCase
import com.example.third_dz.domain.usecase.universe.ObserveVehicleDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val observeVehicleDetail: ObserveVehicleDetailUseCase,
    private val togglePin: TogglePinUseCase,
    private val filmsRepository: GhibliFilmsRepository
) : ViewModel() {

    private val vehicleId = MutableStateFlow("")

    private val _vehicle = MutableStateFlow<Vehicle?>(null)
    val vehicle: StateFlow<Vehicle?> = _vehicle.asStateFlow()

    val filmTitles: StateFlow<Map<String, String>> = _vehicle
        .flatMapLatest { v ->
            if (v == null) flowOf(emptyMap())
            else {
                val filmIds = v.films.map { url -> url.substringAfterLast('/') }.toSet()
                filmsRepository.getFilmsFlow().map { films ->
                    films.filter { it.id in filmIds }.associate { it.id to it.title }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            vehicleId.flatMapLatest { id -> observeVehicleDetail(id) }
                .collect { _vehicle.value = it }
        }
    }

    fun loadVehicle(id: String) {
        vehicleId.value = id
    }

    fun onTogglePin(note: String? = null) {
        _vehicle.value?.let { v ->
            viewModelScope.launch { togglePin(PinnedType.VEHICLE, v.id, note) }
        }
    }
}
