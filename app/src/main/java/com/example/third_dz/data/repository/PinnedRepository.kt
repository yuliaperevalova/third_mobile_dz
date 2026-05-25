package com.example.third_dz.data.repository

import com.example.third_dz.data.local.PinnedEntity
import com.example.third_dz.data.local.PinnedEntityDao
import com.example.third_dz.data.local.PinnedType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PinnedRepository @Inject constructor(
    private val dao: PinnedEntityDao
) {

    suspend fun pin(type: PinnedType, id: String, note: String? = null, now: Long = System.currentTimeMillis()) {
        dao.pin(PinnedEntity(type, id, note, now))
    }

    suspend fun unpin(type: PinnedType, id: String) {
        dao.unpin(type, id)
    }

    fun observeByType(type: PinnedType): Flow<List<PinnedEntity>> =
        dao.observeByType(type)

    fun observeAll(): Flow<List<PinnedEntity>> =
        dao.observeAll()
}
