package com.example.third_dz.domain.usecase.universe

import com.example.third_dz.data.model.Vehicle
import com.example.third_dz.data.repository.UniverseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVehicleDetailUseCase @Inject constructor(
    private val repository: UniverseRepository
) {
    operator fun invoke(vehicleId: String): Flow<Vehicle?> {
        return repository.observeVehicleById(vehicleId)
    }
}
