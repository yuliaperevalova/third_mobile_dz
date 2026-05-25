package com.example.third_dz.data.local

import androidx.room.TypeConverter

class PinnedTypeConverter {

    @TypeConverter
    fun fromPinnedType(type: PinnedType): String = type.name

    @TypeConverter
    fun toPinnedType(value: String): PinnedType =
        runCatching { PinnedType.valueOf(value) }.getOrDefault(PinnedType.PERSON)
}
