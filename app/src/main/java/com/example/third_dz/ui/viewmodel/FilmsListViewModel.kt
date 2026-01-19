package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilmsListViewModel(
    private val repository: GhibliFilmsRepository
) : ViewModel() {

    private val _favourites = MutableStateFlow<Set<String>>(emptySet())
    fun getFavouritesFlow(): StateFlow<Set<String>> = _favourites.asStateFlow()
    
    private val _uiState = MutableStateFlow<FilmsListUiState>(FilmsListUiState.Loading)
    val uiState: StateFlow<FilmsListUiState> = _uiState.asStateFlow()

    init {
        loadFilms()
    }

    fun onEvent(event: FilmsListEvent) {
        when (event) {
            is FilmsListEvent.Refresh -> loadFilms(forceRefresh = true)
            is FilmsListEvent.ToggleFavourite -> toggleFavourite(event.filmId)
        }
    }

    private fun loadFilms(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = FilmsListUiState.Loading
            try {
                val films = repository.getAllFilms(forceRefresh = forceRefresh)
                val favourites = _favourites.value
                _uiState.value = if (films.isEmpty()) {
                    FilmsListUiState.Empty
                } else {
                    FilmsListUiState.Success(films, favourites)
                }
            } catch (e: Exception) {
                _uiState.value = FilmsListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun toggleFavourite(filmId: String) {
        val currentFavourites = _favourites.value.toMutableSet()
        if (currentFavourites.contains(filmId)) {
            currentFavourites.remove(filmId)
        } else {
            currentFavourites.add(filmId)
        }
        _favourites.value = currentFavourites
        
        val currentState = _uiState.value
        if (currentState is FilmsListUiState.Success) {
            _uiState.value = currentState.copy(favourites = _favourites.value)
        }
    }

    fun getFavourites(): Set<String> = _favourites.value
}

