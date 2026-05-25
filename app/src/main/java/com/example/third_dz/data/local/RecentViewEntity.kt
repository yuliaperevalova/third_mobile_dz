package com.example.third_dz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_view")
data class RecentViewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filmId: String,
    val openedAt: Long
)
