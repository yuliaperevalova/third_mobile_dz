package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.third_dz.data.model.Film

@Entity(tableName = "film_cache")
data class FilmEntity(
    @PrimaryKey val id: String,
    val title: String,
    val original_title: String,
    val original_title_romanised: String,
    val description: String,
    val director: String,
    val producer: String,
    val release_date: String,
    val running_time: String,
    val rt_score: String,
    val url: String
)

fun FilmEntity.toFilm() = Film(
    id = id,
    title = title,
    original_title = original_title,
    original_title_romanised = original_title_romanised,
    description = description,
    director = director,
    producer = producer,
    release_date = release_date,
    running_time = running_time,
    rt_score = rt_score,
    people = emptyList(),
    species = emptyList(),
    locations = emptyList(),
    vehicles = emptyList(),
    url = url
)

fun Film.toFilmEntity() = FilmEntity(
    id = id,
    title = title,
    original_title = original_title,
    original_title_romanised = original_title_romanised,
    description = description,
    director = director,
    producer = producer,
    release_date = release_date,
    running_time = running_time,
    rt_score = rt_score,
    url = url
)
