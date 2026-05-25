package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentViewDao {
    @Insert
    suspend fun insert(entity: RecentViewEntity)

    @Query("SELECT * FROM recent_view ORDER BY openedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentViewEntity>>

    @Query("SELECT * FROM recent_view WHERE filmId = :filmId AND openedAt > :since ORDER BY openedAt DESC LIMIT 1")
    suspend fun findRecentByFilmId(filmId: String, since: Long): RecentViewEntity?

    @Query("DELETE FROM recent_view WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recent_view")
    suspend fun clear()
}
