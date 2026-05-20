package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private sealed interface NetworkState {
    data object Loading : NetworkState
    data object Idle : NetworkState
    data class Error(val message: String) : NetworkState
}

@HiltViewModel
class FilmsListViewModel @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val recordRepository: UserFilmRecordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<WatchStatus?>(null)
    val statusFilter: StateFlow<WatchStatus?> = _statusFilter.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val networkState: Flow<NetworkState> = _refreshTrigger
        .flatMapLatest {
            flow {
                emit(NetworkState.Loading)
                try {
                    repository.refreshFilms()
                    emit(NetworkState.Idle)
                } catch (e: Exception) {
                    emit(NetworkState.Error(e.message ?: "Network error"))
                }
            }
        }

    val uiState: StateFlow<FilmsListUiState> = combine(
        repository.getFilmsFlow(),
        recordRepository.observeAll().map { list -> list.associateBy { it.filmId } },
        _searchQuery.debounce(300).distinctUntilChanged(),
        _statusFilter,
        networkState
    ) { films, records, query, statusFilter, network ->
        val byQuery = if (query.isBlank()) films
                      else films.filter { it.title.contains(query, ignoreCase = true) }
        val filtered = if (statusFilter == null) byQuery
                       else byQuery.filter { records[it.id]?.status == statusFilter }
        when {
            network is NetworkState.Error && films.isEmpty() ->
                FilmsListUiState.Error(network.message)
            network is NetworkState.Loading && films.isEmpty() ->
                FilmsListUiState.Loading
            filtered.isEmpty() -> FilmsListUiState.Empty
            else -> FilmsListUiState.Success(
                films = filtered,
                records = records,
                isRefreshing = network is NetworkState.Loading
            )
        }
    }
    .catch { e -> emit(FilmsListUiState.Error(e.message ?: "Unknown error")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilmsListUiState.Loading)

    fun onEvent(event: FilmsListEvent) {
        when (event) {
            is FilmsListEvent.Refresh -> _refreshTrigger.tryEmit(Unit)
            is FilmsListEvent.SearchQueryChanged -> _searchQuery.value = event.query
            is FilmsListEvent.StatusFilterChanged -> _statusFilter.value = event.status
        }
    }
}
