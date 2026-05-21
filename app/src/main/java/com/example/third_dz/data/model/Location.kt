package com.example.third_dz.data.model

data class Location(
    val id: String,
    val name: String,
    val climate: String,
    val terrain: String,
    val surface_water: String,
    val residents: List<String>,
    val films: List<String>,
    val url: String
)
