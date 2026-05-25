package com.example.third_dz.domain.model

import com.example.third_dz.data.local.UserFilmRecordEntity
import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.model.Film

data class UserFilmRecord(
    val filmId: String,
    val status: WatchStatus,
    val rating: Int?,
    val note: String?,
    val watchedAt: Long?,
    val updatedAt: Long
)

data class FilmWithRecord(
    val film: Film,
    val record: UserFilmRecord?
)

fun UserFilmRecordEntity.toDomain() = UserFilmRecord(
    filmId = filmId,
    status = status,
    rating = rating,
    note = note,
    watchedAt = watchedAt,
    updatedAt = updatedAt
)

fun UserFilmRecord.toEntity() = UserFilmRecordEntity(
    filmId = filmId,
    status = status,
    rating = rating,
    note = note,
    watchedAt = watchedAt,
    updatedAt = updatedAt
)
