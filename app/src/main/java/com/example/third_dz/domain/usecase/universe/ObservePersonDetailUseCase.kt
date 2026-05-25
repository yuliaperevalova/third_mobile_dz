package com.example.third_dz.domain.usecase.universe

import com.example.third_dz.data.model.Person
import com.example.third_dz.data.repository.UniverseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePersonDetailUseCase @Inject constructor(
    private val repository: UniverseRepository
) {
    operator fun invoke(personId: String): Flow<Person?> {
        return repository.observePersonById(personId)
    }
}
