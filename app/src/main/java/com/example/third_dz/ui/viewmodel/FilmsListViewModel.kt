package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilmsListViewModel @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val favouriteDao: FavouriteFilmDao
) : ViewModel() {

    private val _favouritesFlow: StateFlow<Set<String>> = favouriteDao.getAllFavourites()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun getFavouritesFlow(): StateFlow<Set<String>> = _favouritesFlow

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
                val favourites = _favouritesFlow.value
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
        viewModelScope.launch {
            if (favouriteDao.isFavourite(filmId)) {
                favouriteDao.delete(filmId)
            } else {
                val film = repository.getFilmById(filmId)
                favouriteDao.insert(
                    FavouriteFilmEntity(
                        id = film.id,
                        title = film.title,
                        original_title = film.original_title,
                        original_title_romanised = film.original_title_romanised,
                        description = film.description,
                        director = film.director,
                        producer = film.producer,
                        release_date = film.release_date,
                        running_time = film.running_time,
                        rt_score = film.rt_score,
                        url = film.url
                    )
                )
            }
            val currentState = _uiState.value
            if (currentState is FilmsListUiState.Success) {
                _uiState.value = currentState.copy(favourites = _favouritesFlow.value)
            }
        }
    }

    fun getFavourites(): Set<String> = _favouritesFlow.value
}
