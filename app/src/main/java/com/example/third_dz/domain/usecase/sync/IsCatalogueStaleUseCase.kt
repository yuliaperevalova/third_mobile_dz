package com.example.third_dz.domain.usecase.sync

import com.example.third_dz.data.preferences.SettingsDataStore
import com.example.third_dz.data.repository.GhibliFilmsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class IsCatalogueStaleUseCase @Inject constructor(
    private val repository: GhibliFilmsRepository,
    private val settings: SettingsDataStore
) {
    operator fun invoke(): Flow<Boolean> =
        settings.cacheTtlMinutesFlow.flatMapLatest { ttl ->
            repository.isStale(ttl)
        }
}
