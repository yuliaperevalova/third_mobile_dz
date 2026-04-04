package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.toFavouriteFilmEntity
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private sealed interface NetworkState {
    data object Loading : NetworkState
    data object Idle : NetworkState
    data class Error(val message: String) : NetworkState
}

@HiltViewModel
class FilmsListViewModel @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val favouriteDao: FavouriteFilmDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Loading)

    val uiState: StateFlow<FilmsListUiState> = combine(
        repository.getFilmsFlow(),
        favouriteDao.getAllFavourites().map { list -> list.map { it.id }.toSet() },
        _searchQuery.debounce(300).distinctUntilChanged(),
        _networkState
    ) { films, favourites, query, networkState ->
        val filtered = if (query.isBlank()) films
                       else films.filter { it.title.contains(query, ignoreCase = true) }
        when {
            networkState is NetworkState.Error && films.isEmpty() ->
                FilmsListUiState.Error((networkState as NetworkState.Error).message)
            networkState is NetworkState.Loading && films.isEmpty() ->
                FilmsListUiState.Loading
            filtered.isEmpty() -> FilmsListUiState.Empty
            else -> FilmsListUiState.Success(filtered, favourites, networkState is NetworkState.Loading)
        }
    }
    .catch { e -> emit(FilmsListUiState.Error(e.message ?: "Unknown error")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilmsListUiState.Loading)

    init {
        refresh()
    }

    fun onEvent(event: FilmsListEvent) {
        when (event) {
            is FilmsListEvent.Refresh -> refresh()
            is FilmsListEvent.ToggleFavourite -> toggleFavourite(event.filmId)
            is FilmsListEvent.SearchQueryChanged -> _searchQuery.value = event.query
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _networkState.value = NetworkState.Loading
            try {
                repository.refreshFilms()
                _networkState.value = NetworkState.Idle
            } catch (e: Exception) {
                _networkState.value = NetworkState.Error(e.message ?: "Network error")
            }
        }
    }

    private fun toggleFavourite(filmId: String) {
        viewModelScope.launch {
            if (favouriteDao.isFavourite(filmId)) {
                favouriteDao.delete(filmId)
            } else {
                val film = repository.getFilmById(filmId)
                favouriteDao.insert(film.toFavouriteFilmEntity())
            }
        }
    }
}
