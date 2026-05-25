package com.example.third_dz.ui.viewmodel.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Person
import com.example.third_dz.domain.usecase.pin.TogglePinUseCase
import com.example.third_dz.domain.usecase.universe.ObservePersonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val observePersonDetail: ObservePersonDetailUseCase,
    private val togglePin: TogglePinUseCase
) : ViewModel() {

    private var currentPersonId: String? = null

    fun loadPerson(personId: String): StateFlow<Person?> {
        currentPersonId = personId
        return observePersonDetail(personId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun onTogglePin(note: String? = null) {
        currentPersonId?.let { id ->
            viewModelScope.launch {
                togglePin(PinnedType.PERSON, id, note)
            }
        }
    }
}
