package com.example.third_dz.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.domain.usecase.collection.AddFilmToCollectionUseCase
import com.example.third_dz.domain.usecase.collection.ObserveCollectionsForFilmUseCase
import com.example.third_dz.domain.usecase.collection.ObserveCollectionsUseCase
import com.example.third_dz.domain.usecase.collection.RemoveFilmFromCollectionUseCase
import com.example.third_dz.domain.usecase.record.ObserveFilmRecordUseCase
import com.example.third_dz.domain.usecase.record.SetNoteUseCase
import com.example.third_dz.domain.usecase.record.SetRatingUseCase
import com.example.third_dz.domain.usecase.record.SetWatchStatusUseCase
import com.example.third_dz.domain.usecase.recent.RecordOpenUseCase
import com.example.third_dz.ui.event.FilmDetailEvent
import com.example.third_dz.ui.state.FilmDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class FilmDetailViewModel @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val setStatus: SetWatchStatusUseCase,
    private val setRating: SetRatingUseCase,
    private val setNote: SetNoteUseCase,
    private val observeRecord: ObserveFilmRecordUseCase,
    observeCollections: ObserveCollectionsUseCase,
    private val observeCollectionsForFilm: ObserveCollectionsForFilmUseCase,
    private val addFilmToCollection: AddFilmToCollectionUseCase,
    private val removeFilmFromCollection: RemoveFilmFromCollectionUseCase,
    private val recordOpen: RecordOpenUseCase
) : ViewModel() {

    private sealed interface FilmResult {
        data object Loading : FilmResult
        data object NotFound : FilmResult
        data class Error(val message: String) : FilmResult
        data class Success(val film: Film) : FilmResult
    }

    private val filmIdFlow = MutableStateFlow<String?>(null)
    private val filmResultFlow = MutableStateFlow<FilmResult>(FilmResult.Loading)

    var uiState by mutableStateOf<FilmDetailUiState>(FilmDetailUiState.Loading)
        private set

    val collections: StateFlow<List<CollectionEntity>> = observeCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberIds: StateFlow<Set<Long>> = filmIdFlow
        .filterNotNull()
        .flatMapLatest { id -> observeCollectionsForFilm(id).map { list -> list.map { it.id }.toSet() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        filmIdFlow
            .filterNotNull()
            .flatMapLatest { id ->
                combine(filmResultFlow, observeRecord(id)) { result, record ->
                    when (result) {
                        FilmResult.Loading -> FilmDetailUiState.Loading
                        FilmResult.NotFound -> FilmDetailUiState.Empty
                        is FilmResult.Error -> FilmDetailUiState.Error(result.message)
                        is FilmResult.Success -> FilmDetailUiState.Success(result.film, record)
                    }
                }
            }
            .onEach { uiState = it }
            .launchIn(viewModelScope)
    }

    fun loadFilm(filmId: String) {
        filmIdFlow.value = filmId
        viewModelScope.launch { recordOpen(filmId) }
        viewModelScope.launch {
            filmResultFlow.value = FilmResult.Loading
            try {
                val film = repository.getFilmById(filmId)
                filmResultFlow.value = FilmResult.Success(film)
            } catch (e: HttpException) {
                filmResultFlow.value = if (e.code() == 404) {
                    FilmResult.NotFound
                } else {
                    FilmResult.Error(e.message ?: "Network error")
                }
            } catch (e: Exception) {
                filmResultFlow.value = FilmResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onEvent(event: FilmDetailEvent) {
        when (event) {
            is FilmDetailEvent.Retry -> filmIdFlow.value?.let { loadFilm(it) }
            is FilmDetailEvent.SetStatus -> viewModelScope.launch { setStatus(event.filmId, event.status) }
            is FilmDetailEvent.SetRating -> viewModelScope.launch { setRating(event.filmId, event.rating) }
            is FilmDetailEvent.SetNote -> viewModelScope.launch { setNote(event.filmId, event.note) }
        }
    }

    fun toggleCollection(filmId: String, collectionId: Long, currentlyMember: Boolean) {
        viewModelScope.launch {
            if (currentlyMember) removeFilmFromCollection(collectionId, filmId)
            else addFilmToCollection(collectionId, filmId)
        }
    }
}
