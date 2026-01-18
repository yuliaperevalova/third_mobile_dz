package com.example.third_dz.data.api

import com.example.third_dz.data.model.MeowFactsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CatFactsApi {
    @GET("/")
    suspend fun getRandomFacts(
        @Query("count") count: Int = 20
    ): MeowFactsResponse

    @GET("/")
    suspend fun getFactById(
        @Query("id") id: Int
    ): MeowFactsResponse
}
