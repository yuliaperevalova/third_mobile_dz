package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.third_dz.data.model.Film
import com.example.third_dz.ui.state.FavouritesUiState

class FavouritesViewModel(
    private val listViewModel: FilmsListViewModel
) : ViewModel() {

    fun getFavouritesState(films: List<Film>): FavouritesUiState {
        val favouriteIds = listViewModel.getFavourites()
        val favouriteFilms = films.filter { it.id in favouriteIds }
        return if (favouriteFilms.isEmpty()) {
            FavouritesUiState.Empty
        } else {
            FavouritesUiState.Success(favouriteFilms)
        }
    }

    fun toggleFavourite(filmId: String) {
        listViewModel.toggleFavourite(filmId)
    }

    fun isFavourite(filmId: String): Boolean {
        return listViewModel.isFavourite(filmId)
    }
}

