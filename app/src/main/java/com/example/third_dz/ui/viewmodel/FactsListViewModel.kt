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

    private val favourites = mutableSetOf<String>()

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
        if (favourites.contains(factId)) {
            favourites.remove(factId)
        } else {
            favourites.add(factId)
        }
    }

    fun isFavourite(factId: String): Boolean {
        return favourites.contains(factId)
    }

    fun getFavourites(): Set<String> = favourites.toSet()
}

