package com.example.third_dz.ui.state

import com.example.third_dz.data.model.Film

sealed class FilmsListUiState {
    data object Loading : FilmsListUiState()
    data class Error(val message: String) : FilmsListUiState()
    data object Empty : FilmsListUiState()
    data class Success(
        val films: List<Film>,
        val favourites: Set<String>,
        val isRefreshing: Boolean = false
    ) : FilmsListUiState()
}

sealed class FilmDetailUiState {
    data object Loading : FilmDetailUiState()
    data class Error(val message: String) : FilmDetailUiState()
    data object Empty : FilmDetailUiState()
    data class Success(
        val film: Film,
        val isFavourite: Boolean
    ) : FilmDetailUiState()
}

sealed class FavouritesUiState {
    data object Loading : FavouritesUiState()
    data class Error(val message: String) : FavouritesUiState()
    data object Empty : FavouritesUiState()
    data class Success(val films: List<Film>) : FavouritesUiState()
}

