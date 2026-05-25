package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiclesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<VehicleEntity>)

    @Query("SELECT * FROM vehicle")
    suspend fun getAll(): List<VehicleEntity>

    @Query("SELECT * FROM vehicle WHERE id = :id")
    suspend fun getById(id: String): VehicleEntity?

    @Query("SELECT * FROM vehicle")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicle WHERE id = :id")
    fun observeById(id: String): Flow<VehicleEntity?>
}
