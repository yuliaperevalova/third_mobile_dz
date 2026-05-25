package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.third_dz.data.model.Species

@Entity(tableName = "species")
@TypeConverters(StringListConverter::class)
data class SpeciesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val classification: String,
    val eyeColors: String,
    val hairColors: String,
    val people: List<String>,
    val films: List<String>,
    val url: String
)

fun Species.toEntity() = SpeciesEntity(
    id = id,
    name = name,
    classification = classification,
    eyeColors = eye_colors,
    hairColors = hair_colors,
    people = people,
    films = films,
    url = url
)

fun SpeciesEntity.toSpecies() = Species(
    id = id,
    name = name,
    classification = classification,
    eye_colors = eyeColors,
    hair_colors = hairColors,
    people = people,
    films = films,
    url = url
)
