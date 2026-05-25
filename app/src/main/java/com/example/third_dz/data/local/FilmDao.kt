package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmDao {

    @Query("SELECT * FROM film_cache ORDER BY title ASC")
    fun getFilmsFlow(): Flow<List<FilmEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(films: List<FilmEntity>)

    @Query("UPDATE film_cache SET lastFetchedAt = :now")
    suspend fun updateAllLastFetchedAt(now: Long)

    @Query("SELECT * FROM film_cache WHERE id = :filmId LIMIT 1")
    suspend fun getFilmById(filmId: String): FilmEntity?

    @Query("SELECT COALESCE(MAX(lastFetchedAt), 0) FROM film_cache")
    fun observeMaxFetchedAt(): Flow<Long>
}
