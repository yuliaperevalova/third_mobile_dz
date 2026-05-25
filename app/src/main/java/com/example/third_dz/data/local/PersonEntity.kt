package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.third_dz.data.model.Person

@Entity(tableName = "person")
@TypeConverters(StringListConverter::class)
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val gender: String,
    val age: String,
    val eyeColor: String,
    val hairColor: String,
    val films: List<String>,
    val species: String,
    val url: String
)

fun Person.toEntity() = PersonEntity(
    id = id,
    name = name,
    gender = gender,
    age = age,
    eyeColor = eye_color,
    hairColor = hair_color,
    films = films,
    species = species,
    url = url
)

fun PersonEntity.toPerson() = Person(
    id = id,
    name = name,
    gender = gender,
    age = age,
    eye_color = eyeColor,
    hair_color = hairColor,
    films = films,
    species = species,
    url = url
)
