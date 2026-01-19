package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.ui.event.FavouritesEvent
import com.example.third_dz.ui.state.FavouritesUiState
import com.example.third_dz.ui.state.FilmsListUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class FavouritesViewModel(
    private val listViewModel: FilmsListViewModel
) : ViewModel() {

    val uiState: StateFlow<FavouritesUiState> = combine(
        listViewModel.uiState,
        listViewModel.getFavouritesFlow()
    ) { listState, favourites ->
        when (listState) {
            is FilmsListUiState.Loading -> FavouritesUiState.Loading
            is FilmsListUiState.Error -> FavouritesUiState.Error(listState.message)
            is FilmsListUiState.Success -> {
                val favouriteFilms = listState.films.filter { it.id in favourites }
                if (favouriteFilms.isEmpty()) {
                    FavouritesUiState.Empty
                } else {
                    FavouritesUiState.Success(favouriteFilms)
                }
            }
            is FilmsListUiState.Empty -> FavouritesUiState.Empty
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = FavouritesUiState.Loading
    )

    fun onEvent(event: FavouritesEvent) {
        when (event) {
            is FavouritesEvent.Retry -> {
                listViewModel.onEvent(com.example.third_dz.ui.event.FilmsListEvent.Refresh)
            }
            is FavouritesEvent.ToggleFavourite -> {
                listViewModel.onEvent(com.example.third_dz.ui.event.FilmsListEvent.ToggleFavourite(event.filmId))
            }
        }
    }
}

