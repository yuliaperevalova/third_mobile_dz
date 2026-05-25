package com.example.third_dz.data.repository

import com.example.third_dz.data.local.RecentViewDao
import com.example.third_dz.data.local.RecentViewEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecentViewRepository @Inject constructor(
    private val dao: RecentViewDao
) {
    suspend fun recordOpen(filmId: String, now: Long = System.currentTimeMillis()) {
        val fiveMinAgo = now - 5 * 60 * 1000
        val existing = dao.findRecentByFilmId(filmId, fiveMinAgo)
        if (existing == null) {
            dao.insert(RecentViewEntity(filmId = filmId, openedAt = now))
        }
    }

    fun observeRecent(limit: Int): Flow<List<RecentViewEntity>> =
        dao.observeRecent(limit)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clear() = dao.clear()
}
