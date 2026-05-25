package com.example.third_dz.ui.viewmodel.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Person
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.domain.usecase.pin.TogglePinUseCase
import com.example.third_dz.domain.usecase.universe.ObservePersonDetailUseCase
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
class PersonDetailViewModel @Inject constructor(
    private val observePersonDetail: ObservePersonDetailUseCase,
    private val togglePin: TogglePinUseCase,
    private val filmsRepository: GhibliFilmsRepository
) : ViewModel() {

    private val personId = MutableStateFlow("")

    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    val filmTitles: StateFlow<Map<String, String>> = _person
        .flatMapLatest { p ->
            if (p == null) flowOf(emptyMap())
            else {
                val filmIds = p.films.map { url -> url.substringAfterLast('/') }.toSet()
                filmsRepository.getFilmsFlow().map { films ->
                    films.filter { it.id in filmIds }.associate { it.id to it.title }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            personId.flatMapLatest { id -> observePersonDetail(id) }
                .collect { _person.value = it }
        }
    }

    fun loadPerson(id: String) {
        personId.value = id
    }

    fun onTogglePin(note: String? = null) {
        _person.value?.let { p ->
            viewModelScope.launch { togglePin(PinnedType.PERSON, p.id, note) }
        }
    }
}
