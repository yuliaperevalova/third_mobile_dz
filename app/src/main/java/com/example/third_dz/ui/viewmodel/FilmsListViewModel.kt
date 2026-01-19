package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.state.FilmsListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilmsListViewModel(
    private val repository: GhibliFilmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilmsListUiState>(FilmsListUiState.Loading)
    val uiState: StateFlow<FilmsListUiState> = _uiState.asStateFlow()

    private val _favourites = MutableStateFlow<Set<String>>(emptySet())
    val favourites: StateFlow<Set<String>> = _favourites.asStateFlow()

    init {
        loadFilms()
    }

    fun loadFilms(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = FilmsListUiState.Loading
            try {
                val films = repository.getAllFilms(forceRefresh = forceRefresh)
                _uiState.value = if (films.isEmpty()) {
                    FilmsListUiState.Empty
                } else {
                    FilmsListUiState.Success(films)
                }
            } catch (e: Exception) {
                _uiState.value = FilmsListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavourite(filmId: String) {
        val currentFavourites = _favourites.value.toMutableSet()
        if (currentFavourites.contains(filmId)) {
            currentFavourites.remove(filmId)
        } else {
            currentFavourites.add(filmId)
        }
        _favourites.value = currentFavourites
    }

    fun isFavourite(filmId: String): Boolean {
        return _favourites.value.contains(filmId)
    }

    fun getFavourites(): Set<String> = _favourites.value
}

