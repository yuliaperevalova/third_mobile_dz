package com.example.third_dz.ui.state

import com.example.third_dz.data.model.Film
import com.example.third_dz.domain.model.UserFilmRecord

sealed class FilmsListUiState {
    data object Loading : FilmsListUiState()
    data class Error(val message: String) : FilmsListUiState()
    data object Empty : FilmsListUiState()
    data class Success(
        val films: List<Film>,
        val records: Map<String, UserFilmRecord>,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val isStale: Boolean = false
    ) : FilmsListUiState()
}

sealed class FilmDetailUiState {
    data object Loading : FilmDetailUiState()
    data class Error(val message: String) : FilmDetailUiState()
    data object Empty : FilmDetailUiState()
    data class Success(
        val film: Film,
        val record: UserFilmRecord?
    ) : FilmDetailUiState()
}



