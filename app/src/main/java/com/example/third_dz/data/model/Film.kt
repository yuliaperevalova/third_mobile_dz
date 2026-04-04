package com.example.third_dz.data.model

data class Film(
    val id: String,
    val title: String,
    val original_title: String,
    val original_title_romanised: String,
    val description: String,
    val director: String,
    val producer: String,
    val release_date: String,
    val running_time: String,
    val rt_score: String,
    val people: List<String>,
    val species: List<String>,
    val locations: List<String>,
    val vehicles: List<String>,
    val url: String
)

