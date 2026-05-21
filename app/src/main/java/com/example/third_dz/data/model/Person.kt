package com.example.third_dz.data.model

data class Person(
    val id: String,
    val name: String,
    val gender: String,
    val age: String,
    val eye_color: String,
    val hair_color: String,
    val films: List<String>,
    val species: String,
    val url: String
)
