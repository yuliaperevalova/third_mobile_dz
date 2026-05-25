package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.domain.usecase.collection.CreateCollectionUseCase
import com.example.third_dz.domain.usecase.collection.DeleteCollectionUseCase
import com.example.third_dz.domain.usecase.collection.ObserveCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsListViewModel @Inject constructor(
    observeCollections: ObserveCollectionsUseCase,
    private val createCollection: CreateCollectionUseCase,
    private val deleteCollection: DeleteCollectionUseCase
) : ViewModel() {

    val collections: StateFlow<List<CollectionEntity>> = observeCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun create(name: String, colorHex: String) {
        viewModelScope.launch {
            runCatching { createCollection(name, colorHex) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { deleteCollection(id) }
    }
}
