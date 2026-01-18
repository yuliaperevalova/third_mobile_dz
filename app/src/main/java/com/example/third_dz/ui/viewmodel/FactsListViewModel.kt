package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.CatFactsRepository
import com.example.third_dz.ui.state.FactsListUiState
import kotlinx.coroutines.launch

class FactsListViewModel(
    private val repository: CatFactsRepository
) : ViewModel() {

    var uiState: FactsListUiState = FactsListUiState.Loading
        private set

    private val favourites = mutableSetOf<String>()

    init {
        loadFacts()
    }

    fun loadFacts(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = FactsListUiState.Loading
            try {
                val facts = repository.getRandomFacts(amount = 20, forceRefresh = forceRefresh)
                uiState = if (facts.isEmpty()) {
                    FactsListUiState.Empty
                } else {
                    FactsListUiState.Success(facts)
                }
            } catch (e: Exception) {
                uiState = FactsListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavourite(factId: String) {
        if (favourites.contains(factId)) {
            favourites.remove(factId)
        } else {
            favourites.add(factId)
        }
        updateUiStateWithFavourites()
    }

    fun isFavourite(factId: String): Boolean {
        return favourites.contains(factId)
    }

    private fun updateUiStateWithFavourites() {
        val currentState = uiState
        if (currentState is FactsListUiState.Success) {
            uiState = currentState
        }
    }

    fun getFavourites(): Set<String> = favourites.toSet()
}

