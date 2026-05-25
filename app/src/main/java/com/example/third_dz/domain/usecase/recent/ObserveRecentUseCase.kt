package com.example.third_dz.domain.usecase.recent

import com.example.third_dz.data.local.RecentViewEntity
import com.example.third_dz.data.preferences.SettingsDataStore
import com.example.third_dz.data.repository.RecentViewRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveRecentUseCase @Inject constructor(
    private val repository: RecentViewRepository,
    private val settings: SettingsDataStore
) {
    operator fun invoke(): Flow<List<RecentViewEntity>> {
        return settings.historyLimitFlow.flatMapLatest { limit ->
            repository.observeRecent(limit)
        }
    }
}
