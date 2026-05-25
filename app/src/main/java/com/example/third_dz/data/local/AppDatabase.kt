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
        CollectionFilmCrossRef::class,
        PersonEntity::class,
        LocationEntity::class,
        SpeciesEntity::class,
        VehicleEntity::class,
        FilmPersonCrossRef::class,
        FilmLocationCrossRef::class,
        FilmSpeciesCrossRef::class,
        FilmVehicleCrossRef::class,
        PinnedEntity::class,
        RecentViewEntity::class
    ],
    version = 6
)
@TypeConverters(WatchStatusConverter::class, StringListConverter::class, PinnedTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteFilmDao(): FavouriteFilmDao
    abstract fun filmDao(): FilmDao
    abstract fun userFilmRecordDao(): UserFilmRecordDao
    abstract fun collectionDao(): CollectionDao
    abstract fun peopleDao(): PeopleDao
    abstract fun locationsDao(): LocationsDao
    abstract fun speciesDao(): SpeciesDao
    abstract fun vehiclesDao(): VehiclesDao
    abstract fun pinnedEntityDao(): PinnedEntityDao
    abstract fun recentViewDao(): RecentViewDao
}
