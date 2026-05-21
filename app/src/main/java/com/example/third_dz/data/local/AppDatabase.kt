package com.example.third_dz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FavouriteFilmEntity::class,
        FilmEntity::class,
        UserFilmRecordEntity::class,
        CollectionEntity::class,
        CollectionFilmCrossRef::class
    ],
    version = 4
)
@TypeConverters(WatchStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteFilmDao(): FavouriteFilmDao
    abstract fun filmDao(): FilmDao
    abstract fun userFilmRecordDao(): UserFilmRecordDao
    abstract fun collectionDao(): CollectionDao
}
