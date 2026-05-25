package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.third_dz.data.model.Vehicle

@Entity(tableName = "vehicle")
@TypeConverters(StringListConverter::class)
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val vehicleClass: String,
    val length: String,
    val pilot: String,
    val films: List<String>,
    val url: String
)

fun Vehicle.toEntity() = VehicleEntity(
    id = id,
    name = name,
    description = description,
    vehicleClass = vehicle_class,
    length = length,
    pilot = pilot,
    films = films,
    url = url
)

fun VehicleEntity.toVehicle() = Vehicle(
    id = id,
    name = name,
    description = description,
    vehicle_class = vehicleClass,
    length = length,
    pilot = pilot,
    films = films,
    url = url
)
