package com.example.third_dz.data.api

import com.example.third_dz.data.model.Film
import com.example.third_dz.data.model.Location
import com.example.third_dz.data.model.Person
import com.example.third_dz.data.model.Species
import com.example.third_dz.data.model.Vehicle
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

    @GET("/people")
    suspend fun getAllPeople(
        @Query("limit") limit: Int? = null
    ): List<Person>

    @GET("/people/{id}")
    suspend fun getPersonById(@Path("id") id: String): Person

    @GET("/locations")
    suspend fun getAllLocations(
        @Query("limit") limit: Int? = null
    ): List<Location>

    @GET("/locations/{id}")
    suspend fun getLocationById(@Path("id") id: String): Location

    @GET("/species")
    suspend fun getAllSpecies(
        @Query("limit") limit: Int? = null
    ): List<Species>

    @GET("/species/{id}")
    suspend fun getSpeciesById(@Path("id") id: String): Species

    @GET("/vehicles")
    suspend fun getAllVehicles(
        @Query("limit") limit: Int? = null
    ): List<Vehicle>

    @GET("/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: String): Vehicle
}
