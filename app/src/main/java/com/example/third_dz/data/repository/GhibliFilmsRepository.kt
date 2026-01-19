package com.example.third_dz.data.repository

import com.example.third_dz.data.api.GhibliFilmsApi
import com.example.third_dz.data.model.Film

class GhibliFilmsRepository(
    private val api: GhibliFilmsApi
) {
    private var cachedFilms: List<Film>? = null

    suspend fun getAllFilms(forceRefresh: Boolean = false): List<Film> {
        return if (!forceRefresh && cachedFilms != null) {
            cachedFilms!!
        } else {
            val films = api.getAllFilms()
            cachedFilms = films
            films
        }
    }

    suspend fun getFilmById(filmId: String): Film {
        cachedFilms?.firstOrNull { it.id == filmId }?.let {
            return it
        }
        
        return api.getFilmById(filmId)
    }

    fun getCachedFilms(): List<Film>? = cachedFilms
}

