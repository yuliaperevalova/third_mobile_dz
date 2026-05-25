package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.RecentViewRepository
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import com.example.third_dz.domain.usecase.sync.IsCatalogueStaleUseCase
import com.example.third_dz.ui.event.FilmsListEvent
import com.example.third_dz.ui.state.FilmsListUiState
import com.example.third_dz.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class FilmsListViewModel @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val recordRepository: UserFilmRecordRepository,
    pinnedRepository: com.example.third_dz.data.repository.PinnedRepository,
    private val recentViewRepository: RecentViewRepository,
    private val networkMonitor: NetworkMonitor,
    private val isCatalogueStaleUseCase: IsCatalogueStaleUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<WatchStatus?>(null)
    val statusFilter: StateFlow<WatchStatus?> = _statusFilter.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private data class ConnectivityState(val isOnline: Boolean, val isStale: Boolean)
    private data class CacheData(
        val films: List<Film>,
        val records: Map<String, UserFilmRecord>,
        val query: String,
        val statusFilter: WatchStatus?,
        val network: NetworkState
    )

    private val connectivityState: Flow<ConnectivityState> = combine(
        networkMonitor.isOnline,
        isCatalogueStaleUseCase()
    ) { isOnline, isStale -> ConnectivityState(isOnline, isStale) }

    val pinnedEntities = pinnedRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentItems: StateFlow<List<HistoryItem>> = combine(
        recentViewRepository.observeRecent(10),
        repository.getFilmsFlow()
    ) { recentViews, films ->
        val filmMap = films.associateBy { it.id }
        recentViews.map { entity ->
            HistoryItem(
                id = entity.id,
                filmId = entity.filmId,
                filmTitle = filmMap[entity.filmId]?.title ?: entity.filmId,
                openedAt = entity.openedAt
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        combine(
            repository.getFilmsFlow(),
            recordRepository.observeAll().map { list -> list.associateBy { it.filmId } },
            _searchQuery.debounce(300).distinctUntilChanged(),
            _statusFilter,
            networkState
        ) { films, records, query, filter, network ->
            CacheData(films = films, records = records, query = query, statusFilter = filter, network = network)
        },
        connectivityState
    ) { data, connectivity ->
        val byQuery = if (data.query.isBlank()) data.films
                      else data.films.filter { it.title.contains(data.query, ignoreCase = true) }
        val filtered = if (data.statusFilter == null) byQuery
                       else byQuery.filter { data.records[it.id]?.status == data.statusFilter }
        when {
            data.network is NetworkState.Error && data.films.isEmpty() ->
                FilmsListUiState.Error(data.network.message)
            data.network is NetworkState.Loading && data.films.isEmpty() ->
                FilmsListUiState.Loading
            filtered.isEmpty() -> FilmsListUiState.Empty
            else -> FilmsListUiState.Success(
                films = filtered,
                records = data.records,
                isRefreshing = data.network is NetworkState.Loading,
                isOffline = !connectivity.isOnline,
                isStale = connectivity.isStale
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
