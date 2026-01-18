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
            val response = api.getRandomFacts(count = amount)
            val facts = response.data.mapIndexed { index, text ->
                Fact.fromText(text, index)
            }
            cachedFacts = facts
            facts
        }
    }

    suspend fun getFactById(factId: String): Fact {
        cachedFacts?.firstOrNull { it.id == factId }?.let {
            return it
        }
        
        val id = factId.toIntOrNull()
        return if (id != null) {
            val response = api.getFactById(id = id)
            val factText = response.data.firstOrNull() ?: throw IllegalArgumentException("Fact not found")
            Fact.fromText(factText, id)
        } else {
            throw IllegalArgumentException("Invalid fact ID: $factId")
        }
    }

    fun getCachedFacts(): List<Fact>? = cachedFacts
}
