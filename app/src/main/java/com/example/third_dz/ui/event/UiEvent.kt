package com.example.third_dz.ui.event

import com.example.third_dz.data.local.WatchStatus

sealed class FilmsListEvent {
    data object Refresh : FilmsListEvent()
    data class SearchQueryChanged(val query: String) : FilmsListEvent()
    data class StatusFilterChanged(val status: WatchStatus?) : FilmsListEvent()
}

sealed class FilmDetailEvent {
    data object Retry : FilmDetailEvent()
    data class SetStatus(val filmId: String, val status: WatchStatus) : FilmDetailEvent()
    data class SetRating(val filmId: String, val rating: Int?) : FilmDetailEvent()
    data class SetNote(val filmId: String, val note: String?) : FilmDetailEvent()
}



