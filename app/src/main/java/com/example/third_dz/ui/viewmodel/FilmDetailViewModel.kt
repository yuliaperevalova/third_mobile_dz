package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.toFavouriteFilmEntity
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.event.FilmDetailEvent
import com.example.third_dz.ui.state.FilmDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilmDetailViewModel @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val favouriteDao: FavouriteFilmDao
) : ViewModel() {

    private var currentFilmId: String? = null

    private val _uiState = MutableStateFlow<FilmDetailUiState>(FilmDetailUiState.Loading)
    val uiState: StateFlow<FilmDetailUiState> = _uiState.asStateFlow()

    init {
        favouriteDao.getAllFavourites()
            .map { list -> list.map { it.id }.toSet() }
            .onEach { favourites ->
                val currentState = _uiState.value
                val filmId = currentFilmId
                if (currentState is FilmDetailUiState.Success && filmId != null) {
                    _uiState.value = currentState.copy(isFavourite = filmId in favourites)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: FilmDetailEvent) {
        when (event) {
            is FilmDetailEvent.Retry -> currentFilmId?.let { loadFilm(it) }
            is FilmDetailEvent.ToggleFavourite -> {
                viewModelScope.launch {
                    if (favouriteDao.isFavourite(event.filmId)) {
                        favouriteDao.delete(event.filmId)
                    } else {
                        val film = repository.getFilmById(event.filmId)
                        favouriteDao.insert(film.toFavouriteFilmEntity())
                    }
                }
            }
        }
    }

    fun loadFilm(filmId: String) {
        currentFilmId = filmId
        viewModelScope.launch {
            _uiState.value = FilmDetailUiState.Loading
            try {
                val film = repository.getFilmById(filmId)
                val isFavourite = favouriteDao.isFavourite(filmId)
                _uiState.value = FilmDetailUiState.Success(film, isFavourite)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                if (errorMessage.contains("404", ignoreCase = true) ||
                    errorMessage.contains("not found", ignoreCase = true)) {
                    _uiState.value = FilmDetailUiState.Empty
                } else {
                    _uiState.value = FilmDetailUiState.Error(errorMessage)
                }
            }
        }
    }
}
