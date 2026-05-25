package com.example.third_dz.domain.usecase.record

import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetWatchStatusUseCase @Inject constructor(
    private val repository: UserFilmRecordRepository
) {
    suspend operator fun invoke(filmId: String, status: WatchStatus, now: Long = System.currentTimeMillis()) {
        val current = repository.observeByFilm(filmId).first()
        val watchedAt = when {
            status == WatchStatus.WATCHED && current?.status != WatchStatus.WATCHED -> now
            status != WatchStatus.WATCHED -> null
            else -> current?.watchedAt
        }
        val next = UserFilmRecord(
            filmId = filmId,
            status = status,
            rating = current?.rating,
            note = current?.note,
            watchedAt = watchedAt,
            updatedAt = now
        )
        repository.upsert(next)
    }
}
