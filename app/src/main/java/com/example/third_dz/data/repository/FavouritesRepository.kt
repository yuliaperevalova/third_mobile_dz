package com.example.third_dz.data.repository

import com.example.third_dz.data.local.FavouriteFilmDao
import com.example.third_dz.data.local.toFavouriteFilmEntity
import com.example.third_dz.data.model.Film
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavouritesRepository @Inject constructor(
    private val dao: FavouriteFilmDao
) {

    fun getAllFavourites(): Flow<List<Film>> = dao.getAllFavourites().map { entities ->
        entities.map { entity ->
            Film(
                id = entity.id,
                title = entity.title,
                original_title = entity.original_title,
                original_title_romanised = entity.original_title_romanised,
                description = entity.description,
                director = entity.director,
                producer = entity.producer,
                release_date = entity.release_date,
                running_time = entity.running_time,
                rt_score = entity.rt_score,
                people = emptyList(),
                species = emptyList(),
                locations = emptyList(),
                vehicles = emptyList(),
                url = entity.url
            )
        }
    }

    suspend fun addFavourite(film: Film) {
        dao.insert(film.toFavouriteFilmEntity())
    }

    suspend fun removeFavourite(filmId: String) {
        dao.delete(filmId)
    }

    suspend fun isFavourite(filmId: String): Boolean {
        return dao.isFavourite(filmId)
    }
}
