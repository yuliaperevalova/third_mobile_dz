package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.CollectionRepository
import com.example.third_dz.domain.usecase.collection.DeleteCollectionUseCase
import com.example.third_dz.domain.usecase.collection.ObserveFilmsInCollectionUseCase
import com.example.third_dz.domain.usecase.collection.RemoveFilmFromCollectionUseCase
import com.example.third_dz.domain.usecase.collection.RenameCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    collectionRepository: CollectionRepository,
    observeFilms: ObserveFilmsInCollectionUseCase,
    private val renameCollection: RenameCollectionUseCase,
    private val deleteCollection: DeleteCollectionUseCase,
    private val removeFilm: RemoveFilmFromCollectionUseCase
) : ViewModel() {

    val collectionId: Long = savedStateHandle.get<Long>("collectionId") ?: 0L

    val collection: StateFlow<CollectionEntity?> =
        collectionRepository.observeCollection(collectionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val films: StateFlow<List<Film>> = observeFilms(collectionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun rename(name: String) {
        viewModelScope.launch {
            runCatching { renameCollection(collectionId, name) }
        }
    }

    fun delete() {
        viewModelScope.launch { deleteCollection(collectionId) }
    }

    fun removeFilm(filmId: String) {
        viewModelScope.launch { removeFilm(collectionId, filmId) }
    }
}
