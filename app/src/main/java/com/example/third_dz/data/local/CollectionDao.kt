package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Insert
    suspend fun insert(collection: CollectionEntity): Long

    @Update
    suspend fun update(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Query("DELETE FROM collection WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM collection ORDER BY createdAt DESC")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collection WHERE id = :id LIMIT 1")
    fun observeCollection(id: Long): Flow<CollectionEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFilm(ref: CollectionFilmCrossRef)

    @Query("DELETE FROM collection_film_cross_ref WHERE collectionId = :collectionId AND filmId = :filmId")
    suspend fun removeFilm(collectionId: Long, filmId: String)

    @Query(
        """
        SELECT film_cache.* FROM film_cache
        INNER JOIN collection_film_cross_ref ref ON ref.filmId = film_cache.id
        WHERE ref.collectionId = :collectionId
        ORDER BY film_cache.title ASC
        """
    )
    fun observeFilmsInCollection(collectionId: Long): Flow<List<FilmEntity>>

    @Query(
        """
        SELECT collection.* FROM collection
        INNER JOIN collection_film_cross_ref ref ON ref.collectionId = collection.id
        WHERE ref.filmId = :filmId
        ORDER BY collection.createdAt DESC
        """
    )
    fun observeCollectionsForFilm(filmId: String): Flow<List<CollectionEntity>>
}
