package com.example.third_dz.domain.usecase.universe

import com.example.third_dz.data.repository.UniverseRepository
import javax.inject.Inject

class RefreshUniverseUseCase @Inject constructor(
    private val repository: UniverseRepository
) {
    suspend operator fun invoke() {
        repository.refreshAll()
    }
}
