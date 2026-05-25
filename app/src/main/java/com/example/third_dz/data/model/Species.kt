package com.example.third_dz.data.model

data class Species(
    val id: String,
    val name: String,
    val classification: String,
    val eye_colors: String,
    val hair_colors: String,
    val people: List<String>,
    val films: List<String>,
    val url: String
)
