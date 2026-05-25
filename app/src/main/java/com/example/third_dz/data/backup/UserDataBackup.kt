package com.example.third_dz.data.backup

data class UserDataBackup(
    val version: Int = 1,
    val timestamp: Long,
    val records: List<BackupRecord>,
    val collections: List<BackupCollection>,
    val pins: List<BackupPin>,
    val recentViews: List<BackupRecentView>
)

data class BackupRecord(
    val filmId: String,
    val status: String,
    val rating: Int?,
    val note: String?,
    val watchedAt: Long?,
    val updatedAt: Long
)

data class BackupCollection(
    val id: Long,
    val name: String,
    val colorHex: String,
    val createdAt: Long,
    val filmIds: List<String>
)

data class BackupPin(
    val entityType: String,
    val entityId: String,
    val note: String?,
    val pinnedAt: Long
)

data class BackupRecentView(
    val filmId: String,
    val openedAt: Long
)
