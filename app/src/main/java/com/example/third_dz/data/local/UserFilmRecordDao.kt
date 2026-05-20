package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFilmRecordDao {

    @Upsert
    suspend fun upsert(record: UserFilmRecordEntity)

    @Query("DELETE FROM user_film_record WHERE filmId = :filmId")
    suspend fun delete(filmId: String)

    @Query("SELECT * FROM user_film_record")
    fun observeAll(): Flow<List<UserFilmRecordEntity>>

    @Query("SELECT * FROM user_film_record WHERE filmId = :filmId LIMIT 1")
    fun observeByFilm(filmId: String): Flow<UserFilmRecordEntity?>
}
