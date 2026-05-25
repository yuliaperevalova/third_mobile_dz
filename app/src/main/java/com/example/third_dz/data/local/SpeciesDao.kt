package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeciesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(species: List<SpeciesEntity>)

    @Query("SELECT * FROM species")
    suspend fun getAll(): List<SpeciesEntity>

    @Query("SELECT * FROM species WHERE id = :id")
    suspend fun getById(id: String): SpeciesEntity?

    @Query("SELECT * FROM species")
    fun observeAll(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE id = :id")
    fun observeById(id: String): Flow<SpeciesEntity?>
}
