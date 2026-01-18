package com.example.third_dz.data.model

import java.util.UUID

data class Fact(
    val id: String,
    val text: String
) {
    companion object {
        fun fromText(text: String, index: Int? = null): Fact {
            return Fact(
                id = index?.toString() ?: UUID.randomUUID().toString(),
                text = text
            )
        }
    }
}
