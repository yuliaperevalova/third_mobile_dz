package com.example.third_dz.domain.usecase.collection

import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCollectionsForFilmUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    operator fun invoke(filmId: String): Flow<List<CollectionEntity>> =
        repository.observeCollectionsForFilm(filmId)
}
