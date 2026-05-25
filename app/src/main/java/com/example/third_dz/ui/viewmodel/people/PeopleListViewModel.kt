package com.example.third_dz.ui.viewmodel.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.model.Person
import com.example.third_dz.data.repository.UniverseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PeopleListViewModel @Inject constructor(
    repository: UniverseRepository
) : ViewModel() {

    val people: StateFlow<List<Person>> = repository.observeAllPeople()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
