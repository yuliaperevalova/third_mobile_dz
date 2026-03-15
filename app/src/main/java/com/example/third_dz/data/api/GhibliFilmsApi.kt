package com.example.third_dz.data.api

import com.example.third_dz.data.model.Film
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GhibliFilmsApi {
    @GET("/films")
    suspend fun getAllFilms(
        @Query("limit") limit: Int? = null,
        @Query("fields") fields: String? = null
    ): List<Film>

    @GET("/films/{id}")
    suspend fun getFilmById(
        @Path("id") id: String,
        @Query("fields") fields: String? = null
    ): Film
}

