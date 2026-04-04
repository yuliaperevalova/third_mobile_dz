package com.example.third_dz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavouriteFilmEntity::class, FilmEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteFilmDao(): FavouriteFilmDao
    abstract fun filmDao(): FilmDao
}
