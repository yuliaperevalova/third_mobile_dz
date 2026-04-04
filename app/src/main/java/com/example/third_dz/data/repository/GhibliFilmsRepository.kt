package com.example.third_dz.data.repository

import com.example.third_dz.data.api.GhibliFilmsApi
import com.example.third_dz.data.local.FilmDao
import com.example.third_dz.data.local.toFilm
import com.example.third_dz.data.local.toFilmEntity
import com.example.third_dz.data.model.Film
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GhibliFilmsRepository(
    private val api: GhibliFilmsApi,
    private val filmDao: FilmDao
) {
    fun getFilmsFlow(): Flow<List<Film>> = filmDao.getFilmsFlow().map { entities ->
        entities.map { it.toFilm() }
    }

    suspend fun refreshFilms() {
        val films = api.getAllFilms()
        filmDao.insertAll(films.map { it.toFilmEntity() })
    }

    suspend fun getFilmById(filmId: String): Film {
        filmDao.getFilmById(filmId)?.let { return it.toFilm() }
        return api.getFilmById(filmId)
    }
}
