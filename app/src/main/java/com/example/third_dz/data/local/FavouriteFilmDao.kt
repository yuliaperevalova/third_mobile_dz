package com.example.third_dz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteFilmDao {

    @Query("SELECT * FROM favourite_films")
    fun getAllFavourites(): Flow<List<FavouriteFilmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteFilmEntity)

    @Query("DELETE FROM favourite_films WHERE id = :filmId")
    suspend fun delete(filmId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_films WHERE id = :filmId)")
    suspend fun isFavourite(filmId: String): Boolean
}
