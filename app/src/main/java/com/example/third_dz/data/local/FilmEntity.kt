package com.example.third_dz.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.third_dz.data.model.Film

@Entity(tableName = "film_cache")
data class FilmEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "original_title")
    val originalTitle: String,
    @ColumnInfo(name = "original_title_romanised")
    val originalTitleRomanised: String,
    val description: String,
    val director: String,
    val producer: String,
    @ColumnInfo(name = "release_date")
    val releaseDate: String,
    @ColumnInfo(name = "running_time")
    val runningTime: String,
    @ColumnInfo(name = "rt_score")
    val rtScore: String,
    val url: String,
    val lastFetchedAt: Long = 0L
)

fun FilmEntity.toFilm() = Film(
    id = id,
    title = title,
    originalTitle = originalTitle,
    originalTitleRomanised = originalTitleRomanised,
    description = description,
    director = director,
    producer = producer,
    releaseDate = releaseDate,
    runningTime = runningTime,
    rtScore = rtScore,
    people = emptyList(),
    species = emptyList(),
    locations = emptyList(),
    vehicles = emptyList(),
    url = url
)

fun Film.toFilmEntity(lastFetchedAt: Long = 0L) = FilmEntity(
    id = id,
    title = title,
    originalTitle = originalTitle,
    originalTitleRomanised = originalTitleRomanised,
    description = description,
    director = director,
    producer = producer,
    releaseDate = releaseDate,
    runningTime = runningTime,
    rtScore = rtScore,
    url = url,
    lastFetchedAt = lastFetchedAt
)
