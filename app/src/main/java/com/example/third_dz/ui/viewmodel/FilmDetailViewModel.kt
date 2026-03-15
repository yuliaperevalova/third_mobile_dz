package com.example.third_dz.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.toFavouriteFilmEntity
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.event.FilmDetailEvent
import com.example.third_dz.ui.state.FilmDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var uiState by mutableStateOf<FilmDetailUiState>(FilmDetailUiState.Loading)
        private set

    init {
        favouriteDao.getAllFavourites()
            .map { list -> list.map { it.id }.toSet() }
            .onEach { favourites ->
                val currentState = uiState
                val filmId = currentFilmId
                if (currentState is FilmDetailUiState.Success && filmId != null) {
                    uiState = currentState.copy(isFavourite = filmId in favourites)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: FilmDetailEvent) {
        when (event) {
            is FilmDetailEvent.Retry -> currentFilmId?.let { loadFilm(it) }
            is FilmDetailEvent.ToggleFavourite -> {
                val currentState = uiState
                if (currentState !is FilmDetailUiState.Success) return
                viewModelScope.launch {
                    if (currentState.isFavourite) {
                        favouriteDao.delete(event.filmId)
                    } else {
                        favouriteDao.insert(currentState.film.toFavouriteFilmEntity())
                    }
                }
            }
        }
    }

    fun loadFilm(filmId: String) {
        currentFilmId = filmId
        viewModelScope.launch {
            uiState = FilmDetailUiState.Loading
            try {
                val film = repository.getFilmById(filmId)
                val isFavourite = favouriteDao.isFavourite(filmId)
                uiState = FilmDetailUiState.Success(film, isFavourite)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                if (errorMessage.contains("404", ignoreCase = true) ||
                    errorMessage.contains("not found", ignoreCase = true)) {
                    uiState = FilmDetailUiState.Empty
                } else {
                    uiState = FilmDetailUiState.Error(errorMessage)
                }
            }
        }
    }
}
