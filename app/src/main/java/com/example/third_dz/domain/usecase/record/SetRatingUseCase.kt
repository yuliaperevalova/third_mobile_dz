package com.example.third_dz.domain.usecase.record

import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetRatingUseCase @Inject constructor(
    private val repository: UserFilmRecordRepository
) {
    suspend operator fun invoke(filmId: String, rating: Int?, now: Long = System.currentTimeMillis()) {
        require(rating == null || rating in 1..10) { "rating must be 1..10 or null" }
        val current = repository.observeByFilm(filmId).first()
        val next = UserFilmRecord(
            filmId = filmId,
            status = current?.status ?: WatchStatus.PLAN,
            rating = rating,
            note = current?.note,
            watchedAt = current?.watchedAt,
            updatedAt = now
        )
        repository.upsert(next)
    }
}
