package com.example.third_dz.domain.usecase.record

import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetNoteUseCase @Inject constructor(
    private val repository: UserFilmRecordRepository
) {
    suspend operator fun invoke(filmId: String, note: String?, now: Long = System.currentTimeMillis()) {
        val trimmed = note?.trim()?.ifBlank { null }
        val current = repository.observeByFilm(filmId).first()
        val next = UserFilmRecord(
            filmId = filmId,
            status = current?.status ?: WatchStatus.PLAN,
            rating = current?.rating,
            note = trimmed,
            watchedAt = current?.watchedAt,
            updatedAt = now
        )
        repository.upsert(next)
    }
}
