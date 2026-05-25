package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Query("SELECT * FROM location")
    suspend fun getAll(): List<LocationEntity>

    @Query("SELECT * FROM location WHERE id = :id")
    suspend fun getById(id: String): LocationEntity?

    @Query("SELECT * FROM location")
    fun observeAll(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE id = :id")
    fun observeById(id: String): Flow<LocationEntity?>
}
