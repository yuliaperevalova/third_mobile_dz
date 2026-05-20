package com.example.third_dz.data.repository

import com.example.third_dz.data.local.UserFilmRecordDao
import com.example.third_dz.domain.model.UserFilmRecord
import com.example.third_dz.domain.model.toDomain
import com.example.third_dz.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserFilmRecordRepository @Inject constructor(
    private val dao: UserFilmRecordDao
) {

    fun observeAll(): Flow<List<UserFilmRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeByFilm(filmId: String): Flow<UserFilmRecord?> =
        dao.observeByFilm(filmId).map { it?.toDomain() }

    suspend fun upsert(record: UserFilmRecord) {
        dao.upsert(record.toEntity())
    }

    suspend fun delete(filmId: String) {
        dao.delete(filmId)
    }
}
