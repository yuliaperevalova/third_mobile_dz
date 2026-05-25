package com.example.third_dz.domain.usecase.recent

import com.example.third_dz.data.repository.RecentViewRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val repository: RecentViewRepository
) {
    suspend operator fun invoke() {
        repository.clear()
    }
}
