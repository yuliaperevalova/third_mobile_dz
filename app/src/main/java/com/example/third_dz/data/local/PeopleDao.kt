package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(people: List<PersonEntity>)

    @Query("SELECT * FROM person")
    suspend fun getAll(): List<PersonEntity>

    @Query("SELECT * FROM person WHERE id = :id")
    suspend fun getById(id: String): PersonEntity?

    @Query("SELECT * FROM person")
    fun observeAll(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM person WHERE id = :id")
    fun observeById(id: String): Flow<PersonEntity?>
}
