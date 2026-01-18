package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.CatFactsRepository
import com.example.third_dz.ui.state.FactsListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FactsListViewModel(
    private val repository: CatFactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FactsListUiState>(FactsListUiState.Loading)
    val uiState: StateFlow<FactsListUiState> = _uiState.asStateFlow()

    private val _favourites = MutableStateFlow<Set<String>>(emptySet())
    val favourites: StateFlow<Set<String>> = _favourites.asStateFlow()

    init {
        loadFacts()
    }

    fun loadFacts(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = FactsListUiState.Loading
            try {
                val facts = repository.getRandomFacts(amount = 20, forceRefresh = forceRefresh)
                _uiState.value = if (facts.isEmpty()) {
                    FactsListUiState.Empty
                } else {
                    FactsListUiState.Success(facts)
                }
            } catch (e: Exception) {
                _uiState.value = FactsListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavourite(factId: String) {
        val currentFavourites = _favourites.value.toMutableSet()
        if (currentFavourites.contains(factId)) {
            currentFavourites.remove(factId)
        } else {
            currentFavourites.add(factId)
        }
        _favourites.value = currentFavourites
    }

    fun isFavourite(factId: String): Boolean {
        return _favourites.value.contains(factId)
    }

    fun getFavourites(): Set<String> = _favourites.value
}

