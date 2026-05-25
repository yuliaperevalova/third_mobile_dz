package com.example.third_dz.domain.usecase.pin

import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.repository.PinnedRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TogglePinUseCase @Inject constructor(
    private val repository: PinnedRepository
) {
    suspend operator fun invoke(type: PinnedType, id: String, note: String? = null) {
        val current = repository.observeByType(type).first()
        val isPinned = current.any { it.entityId == id }
        
        if (isPinned) {
            repository.unpin(type, id)
        } else {
            repository.pin(type, id, note)
        }
    }
}
