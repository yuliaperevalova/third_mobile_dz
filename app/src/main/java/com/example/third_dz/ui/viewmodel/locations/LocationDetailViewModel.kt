package com.example.third_dz.ui.viewmodel.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Location
import com.example.third_dz.domain.usecase.pin.TogglePinUseCase
import com.example.third_dz.domain.usecase.universe.ObserveLocationDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val observeLocationDetail: ObserveLocationDetailUseCase,
    private val togglePin: TogglePinUseCase
) : ViewModel() {

    private var currentLocationId: String? = null

    fun loadLocation(locationId: String): StateFlow<Location?> {
        currentLocationId = locationId
        return observeLocationDetail(locationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun onTogglePin(note: String? = null) {
        currentLocationId?.let { id ->
            viewModelScope.launch {
                togglePin(PinnedType.LOCATION, id, note)
            }
        }
    }
}
