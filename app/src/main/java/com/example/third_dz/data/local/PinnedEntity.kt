package com.example.third_dz.data.local

import androidx.room.Entity

enum class PinnedType {
    PERSON,
    LOCATION,
    SPECIES,
    VEHICLE
}

@Entity(
    tableName = "pinned_entity",
    primaryKeys = ["entityType", "entityId"]
)
data class PinnedEntity(
    val entityType: PinnedType,
    val entityId: String,
    val note: String?,
    val pinnedAt: Long
)
