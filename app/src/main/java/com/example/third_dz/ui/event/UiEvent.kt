package com.example.third_dz.ui.event

sealed class FilmsListEvent {
    data object Refresh : FilmsListEvent()
    data class ToggleFavourite(val filmId: String) : FilmsListEvent()
}

sealed class FilmDetailEvent {
    data object Retry : FilmDetailEvent()
    data class ToggleFavourite(val filmId: String) : FilmDetailEvent()
}

sealed class FavouritesEvent {
    data object Retry : FavouritesEvent()
    data class ToggleFavourite(val filmId: String) : FavouritesEvent()
}

