package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.FavouriteFilmEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.ui.event.FavouritesEvent
import com.example.third_dz.ui.state.FavouritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouriteDao: FavouriteFilmDao
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val _filmRemovedEvent = MutableSharedFlow<String>()
    val filmRemovedEvent: SharedFlow<String> = _filmRemovedEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<FavouritesUiState> = retryTrigger
        .flatMapLatest {
            combine(
                favouriteDao.getAllFavourites(),
                _searchQuery.debounce(300).distinctUntilChanged()
            ) { entities, query ->
                val filtered = if (query.isBlank()) entities
                               else entities.filter { it.title.contains(query, ignoreCase = true) }
                if (filtered.isEmpty()) FavouritesUiState.Empty
                else FavouritesUiState.Success(filtered.map { it.toFilm() })
            }.catch { e -> emit(FavouritesUiState.Error(e.message ?: "Unknown error")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavouritesUiState.Loading)

    fun onEvent(event: FavouritesEvent) {
        when (event) {
            is FavouritesEvent.ToggleFavourite -> viewModelScope.launch {
                favouriteDao.delete(event.filmId)
                _filmRemovedEvent.emit(event.filmId)
            }
            is FavouritesEvent.Retry -> viewModelScope.launch {
                retryTrigger.emit(Unit)
            }
            is FavouritesEvent.SearchQueryChanged -> _searchQuery.value = event.query
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
