package com.example.third_dz.data.repository

import com.example.third_dz.data.local.CollectionDao
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.local.CollectionFilmCrossRef
import com.example.third_dz.data.local.toFilm
import com.example.third_dz.data.model.Film
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionRepository @Inject constructor(
    private val dao: CollectionDao
) {

    fun observeCollections(): Flow<List<CollectionEntity>> = dao.observeCollections()

    fun observeCollection(id: Long): Flow<CollectionEntity?> = dao.observeCollection(id)

    fun observeFilmsInCollection(collectionId: Long): Flow<List<Film>> =
        dao.observeFilmsInCollection(collectionId).map { list -> list.map { it.toFilm() } }

    fun observeCollectionsForFilm(filmId: String): Flow<List<CollectionEntity>> =
        dao.observeCollectionsForFilm(filmId)

    suspend fun create(name: String, colorHex: String, now: Long = System.currentTimeMillis()): Long {
        return dao.insert(
            CollectionEntity(name = name, colorHex = colorHex, createdAt = now)
        )
    }

    suspend fun rename(id: Long, name: String) {
        val current = dao.observeCollection(id).first() ?: return
        dao.update(current.copy(name = name))
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    suspend fun addFilm(collectionId: Long, filmId: String) {
        dao.addFilm(CollectionFilmCrossRef(collectionId, filmId))
    }

    suspend fun removeFilm(collectionId: Long, filmId: String) {
        dao.removeFilm(collectionId, filmId)
    }
}
