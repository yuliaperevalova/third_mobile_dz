package com.example.third_dz.domain.usecase.collection

import com.example.third_dz.data.model.Film
import com.example.third_dz.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFilmsInCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    operator fun invoke(collectionId: Long): Flow<List<Film>> =
        repository.observeFilmsInCollection(collectionId)
}
