package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.third_dz.data.model.Location

@Entity(tableName = "location")
@TypeConverters(StringListConverter::class)
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val climate: String,
    val terrain: String,
    val surfaceWater: String,
    val residents: List<String>,
    val films: List<String>,
    val url: String
)

fun Location.toEntity() = LocationEntity(
    id = id,
    name = name,
    climate = climate,
    terrain = terrain,
    surfaceWater = surface_water,
    residents = residents,
    films = films,
    url = url
)

fun LocationEntity.toLocation() = Location(
    id = id,
    name = name,
    climate = climate,
    terrain = terrain,
    surface_water = surfaceWater,
    residents = residents,
    films = films,
    url = url
)
