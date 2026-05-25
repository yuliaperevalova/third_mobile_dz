package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pin(entity: PinnedEntity)

    @Query("DELETE FROM pinned_entity WHERE entityType = :type AND entityId = :id")
    suspend fun unpin(type: PinnedType, id: String)

    @Query("SELECT * FROM pinned_entity WHERE entityType = :type ORDER BY pinnedAt DESC")
    fun observeByType(type: PinnedType): Flow<List<PinnedEntity>>

    @Query("SELECT * FROM pinned_entity ORDER BY pinnedAt DESC")
    fun observeAll(): Flow<List<PinnedEntity>>
}
