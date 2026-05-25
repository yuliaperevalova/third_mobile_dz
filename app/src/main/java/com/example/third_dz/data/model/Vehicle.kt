package com.example.third_dz.data.model

data class Vehicle(
    val id: String,
    val name: String,
    val description: String,
    val vehicle_class: String,
    val length: String,
    val pilot: String,
    val films: List<String>,
    val url: String
)
