package com.example.third_dz.data.local

import androidx.room.TypeConverter

enum class WatchStatus { PLAN, WATCHING, WATCHED, DROPPED }

class WatchStatusConverter {

    @TypeConverter
    fun fromStatus(status: WatchStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): WatchStatus =
        runCatching { WatchStatus.valueOf(value) }.getOrDefault(WatchStatus.PLAN)
}
