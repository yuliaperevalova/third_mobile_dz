package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.state.FilmDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilmDetailViewModel(
    private val repository: GhibliFilmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilmDetailUiState>(FilmDetailUiState.Loading)
    val uiState: StateFlow<FilmDetailUiState> = _uiState.asStateFlow()

    fun loadFilm(filmId: String) {
        viewModelScope.launch {
            _uiState.value = FilmDetailUiState.Loading
            try {
                val film = repository.getFilmById(filmId)
                _uiState.value = FilmDetailUiState.Success(film)
            } catch (e: Exception) {
                _uiState.value = FilmDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

