package com.example.third_dz.domain.usecase.collection

import com.example.third_dz.data.repository.CollectionRepository
import javax.inject.Inject

class CreateCollectionUseCase @Inject constructor(
    private val repository: CollectionRepository
) {
    suspend operator fun invoke(name: String, colorHex: String, now: Long = System.currentTimeMillis()): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "collection name cannot be blank" }
        return repository.create(trimmed, colorHex, now)
    }
}
