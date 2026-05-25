package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_film_cross_ref",
    primaryKeys = ["collectionId", "filmId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FilmEntity::class,
            parentColumns = ["id"],
            childColumns = ["filmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("filmId")]
)
data class CollectionFilmCrossRef(
    val collectionId: Long,
    val filmId: String
)
