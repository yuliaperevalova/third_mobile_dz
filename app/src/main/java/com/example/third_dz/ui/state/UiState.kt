package com.example.third_dz.ui.state

import com.example.third_dz.data.model.Film

sealed class FilmsListUiState {
    data object Loading : FilmsListUiState()
    data class Error(val message: String) : FilmsListUiState()
    data object Empty : FilmsListUiState()
    data class Success(val films: List<Film>) : FilmsListUiState()
}

sealed class FilmDetailUiState {
    data object Loading : FilmDetailUiState()
    data class Error(val message: String) : FilmDetailUiState()
    data class Success(val film: Film) : FilmDetailUiState()
}

sealed class FavouritesUiState {
    data object Empty : FavouritesUiState()
    data class Success(val films: List<Film>) : FavouritesUiState()
}

