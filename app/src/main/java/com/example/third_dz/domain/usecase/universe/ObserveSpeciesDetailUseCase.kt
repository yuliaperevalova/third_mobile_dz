package com.example.third_dz.domain.usecase.universe

import com.example.third_dz.data.model.Species
import com.example.third_dz.data.repository.UniverseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSpeciesDetailUseCase @Inject constructor(
    private val repository: UniverseRepository
) {
    operator fun invoke(speciesId: String): Flow<Species?> {
        return repository.observeSpeciesById(speciesId)
    }
}
