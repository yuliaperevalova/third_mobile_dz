package com.example.third_dz.domain.usecase.universe

import com.example.third_dz.data.model.Location
import com.example.third_dz.data.repository.UniverseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocationDetailUseCase @Inject constructor(
    private val repository: UniverseRepository
) {
    operator fun invoke(locationId: String): Flow<Location?> {
        return repository.observeLocationById(locationId)
    }
}
