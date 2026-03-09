package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.third_dz.data.model.Film

@Entity(tableName = "favourite_films")
data class FavouriteFilmEntity(
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
