package com.example.third_dz.ui.event

import com.example.third_dz.data.model.SortOrder

sealed class FilmsListEvent {
    data object Refresh : FilmsListEvent()
    data class ToggleFavourite(val filmId: String) : FilmsListEvent()
    data class SearchQueryChanged(val query: String) : FilmsListEvent()
}

sealed class FilmDetailEvent {
    data object Retry : FilmDetailEvent()
    data class ToggleFavourite(val filmId: String) : FilmDetailEvent()
}

sealed class FavouritesEvent {
    data object Retry : FavouritesEvent()
    data class ToggleFavourite(val filmId: String) : FavouritesEvent()
    data class SearchQueryChanged(val query: String) : FavouritesEvent()
    data class SortOrderChanged(val order: SortOrder) : FavouritesEvent()
}

