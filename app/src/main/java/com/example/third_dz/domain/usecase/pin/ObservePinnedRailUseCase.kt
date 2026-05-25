package com.example.third_dz.domain.usecase.pin

import com.example.third_dz.data.local.PinnedEntity
import com.example.third_dz.data.repository.PinnedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePinnedRailUseCase @Inject constructor(
    private val repository: PinnedRepository
) {
    operator fun invoke(): Flow<List<PinnedEntity>> {
        return repository.observeAll()
    }
}
