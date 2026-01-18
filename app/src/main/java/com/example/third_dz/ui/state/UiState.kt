package com.example.third_dz.ui.state

import com.example.third_dz.data.model.Fact

sealed class FactsListUiState {
    data object Loading : FactsListUiState()
    data class Error(val message: String) : FactsListUiState()
    data object Empty : FactsListUiState()
    data class Success(val facts: List<Fact>) : FactsListUiState()
}

sealed class FactDetailUiState {
    data object Loading : FactDetailUiState()
    data class Error(val message: String) : FactDetailUiState()
    data class Success(val fact: Fact) : FactDetailUiState()
}

sealed class FavouritesUiState {
    data object Empty : FavouritesUiState()
    data class Success(val facts: List<Fact>) : FavouritesUiState()
}

