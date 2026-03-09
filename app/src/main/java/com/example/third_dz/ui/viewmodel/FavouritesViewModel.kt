package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.ui.event.FavouritesEvent
import com.example.third_dz.ui.state.FavouritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouriteDao: FavouriteFilmDao
) : ViewModel() {

    val uiState: StateFlow<FavouritesUiState> = favouriteDao.getAllFavourites()
        .map { entities ->
            if (entities.isEmpty()) {
                FavouritesUiState.Empty
            } else {
                FavouritesUiState.Success(entities.map { it.toFilm() })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavouritesUiState.Loading)

    fun onEvent(event: FavouritesEvent) {
        when (event) {
            is FavouritesEvent.ToggleFavourite -> viewModelScope.launch {
                favouriteDao.delete(event.filmId)
            }
            is FavouritesEvent.Retry -> Unit
        }
    }

    private fun FavouriteFilmEntity.toFilm() = Film(
        id = id,
        title = title,
        original_title = original_title,
        original_title_romanised = original_title_romanised,
        description = description,
        director = director,
        producer = producer,
        release_date = release_date,
        running_time = running_time,
        rt_score = rt_score,
        people = emptyList(),
        species = emptyList(),
        locations = emptyList(),
        vehicles = emptyList(),
        url = url
    )
}
