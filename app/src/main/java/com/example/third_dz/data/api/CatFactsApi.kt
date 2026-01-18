package com.example.third_dz.data.api

import com.example.third_dz.data.model.Fact
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatFactsApi {
    @GET("facts/random")
    suspend fun getRandomFacts(
        @Query("animal_type") animalType: String = "cat",
        @Query("amount") amount: Int = 20
    ): List<Fact>

    @GET("facts/{factId}")
    suspend fun getFactById(
        @Path("factId") factId: String,
        @Query("animal_type") animalType: String = "cat"
    ): Fact
}

