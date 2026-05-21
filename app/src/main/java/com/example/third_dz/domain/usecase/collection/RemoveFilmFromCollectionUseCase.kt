package com.example.third_dz.domain.usecase.collection

import com.example.third_dz.data.repository.CollectionRepository
import javax.inject.Inject

class RemoveFilmFromCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(collectionId: Long, filmId: String) {
        repository.removeFilm(collectionId, filmId)
    }
}
