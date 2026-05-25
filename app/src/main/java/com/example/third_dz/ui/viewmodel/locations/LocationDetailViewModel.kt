package com.example.third_dz.ui.viewmodel.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Location
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.domain.usecase.pin.TogglePinUseCase
import com.example.third_dz.domain.usecase.universe.ObserveLocationDetailUseCase
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
class LocationDetailViewModel @Inject constructor(
    private val observeLocationDetail: ObserveLocationDetailUseCase,
    private val togglePin: TogglePinUseCase,
    private val filmsRepository: GhibliFilmsRepository
) : ViewModel() {

    private val locationId = MutableStateFlow("")

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    val filmTitles: StateFlow<Map<String, String>> = _location
        .flatMapLatest { loc ->
            if (loc == null) flowOf(emptyMap())
            else {
                val filmIds = loc.films.map { url -> url.substringAfterLast('/') }.toSet()
                filmsRepository.getFilmsFlow().map { films ->
                    films.filter { it.id in filmIds }.associate { it.id to it.title }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            locationId.flatMapLatest { id -> observeLocationDetail(id) }
                .collect { _location.value = it }
        }
    }

    fun loadLocation(id: String) {
        locationId.value = id
    }

    fun onTogglePin(note: String? = null) {
        _location.value?.let { loc ->
            viewModelScope.launch { togglePin(PinnedType.LOCATION, loc.id, note) }
        }
    }
}
