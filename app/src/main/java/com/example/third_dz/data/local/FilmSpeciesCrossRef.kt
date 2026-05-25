package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "film_species_cross_ref",
    primaryKeys = ["filmId", "speciesId"],
    foreignKeys = [
        ForeignKey(
            entity = FilmEntity::class,
            parentColumns = ["id"],
            childColumns = ["filmId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("speciesId")]
)
data class FilmSpeciesCrossRef(
    val filmId: String,
    val speciesId: String
)
