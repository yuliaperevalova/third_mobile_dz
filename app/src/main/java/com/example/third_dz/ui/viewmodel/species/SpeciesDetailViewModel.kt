package com.example.third_dz.ui.viewmodel.species

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Species
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.domain.usecase.pin.TogglePinUseCase
import com.example.third_dz.domain.usecase.universe.ObserveSpeciesDetailUseCase
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
class SpeciesDetailViewModel @Inject constructor(
    private val observeSpeciesDetail: ObserveSpeciesDetailUseCase,
    private val togglePin: TogglePinUseCase,
    private val filmsRepository: GhibliFilmsRepository
) : ViewModel() {

    private val speciesId = MutableStateFlow("")

    private val _species = MutableStateFlow<Species?>(null)
    val species: StateFlow<Species?> = _species.asStateFlow()

    val filmTitles: StateFlow<Map<String, String>> = _species
        .flatMapLatest { s ->
            if (s == null) flowOf(emptyMap())
            else {
                val filmIds = s.films.map { url -> url.substringAfterLast('/') }.toSet()
                filmsRepository.getFilmsFlow().map { films ->
                    films.filter { it.id in filmIds }.associate { it.id to it.title }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            speciesId.flatMapLatest { id -> observeSpeciesDetail(id) }
                .collect { _species.value = it }
        }
    }

    fun loadSpecies(id: String) {
        speciesId.value = id
    }

    fun onTogglePin(note: String? = null) {
        _species.value?.let { s ->
            viewModelScope.launch { togglePin(PinnedType.SPECIES, s.id, note) }
        }
    }
}
