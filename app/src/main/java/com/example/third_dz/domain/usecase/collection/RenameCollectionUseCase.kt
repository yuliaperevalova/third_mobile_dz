package com.example.third_dz.domain.usecase.collection

import com.example.third_dz.data.repository.CollectionRepository
import javax.inject.Inject

class RenameCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(id: Long, name: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "collection name cannot be blank" }
        repository.rename(id, trimmed)
    }
}
