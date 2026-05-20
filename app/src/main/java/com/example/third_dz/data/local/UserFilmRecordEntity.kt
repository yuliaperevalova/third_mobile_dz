package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_film_record")
data class UserFilmRecordEntity(
    @PrimaryKey val filmId: String,
    val status: WatchStatus,
    val rating: Int?,
    val note: String?,
    val watchedAt: Long?,
    val updatedAt: Long
)
