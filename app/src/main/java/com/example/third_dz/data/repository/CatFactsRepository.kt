package com.example.third_dz.data.repository

import com.example.third_dz.data.api.CatFactsApi
import com.example.third_dz.data.model.Fact

class CatFactsRepository(
    private val api: CatFactsApi
) {
    private var cachedFacts: List<Fact>? = null

    suspend fun getRandomFacts(amount: Int = 20, forceRefresh: Boolean = false): List<Fact> {
        return if (!forceRefresh && cachedFacts != null) {
            cachedFacts!!
        } else {
            val facts = api.getRandomFacts(amount = amount)
            cachedFacts = facts
            facts
        }
    }

    suspend fun getFactById(factId: String): Fact {
        return api.getFactById(factId)
    }

    fun getCachedFacts(): List<Fact>? = cachedFacts
}

