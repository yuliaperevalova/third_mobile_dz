package com.example.third_dz.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_film_record (
                    filmId TEXT NOT NULL PRIMARY KEY,
                    status TEXT NOT NULL,
                    rating INTEGER,
                    note TEXT,
                    watchedAt INTEGER,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO user_film_record(filmId, status, rating, note, watchedAt, updatedAt)
                SELECT id, 'WATCHED', NULL, NULL, NULL, CAST(strftime('%s','now') AS INTEGER) * 1000
                FROM favourite_films
                """.trimIndent()
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS collection (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    colorHex TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS collection_film_cross_ref (
                    collectionId INTEGER NOT NULL,
                    filmId TEXT NOT NULL,
                    PRIMARY KEY(collectionId, filmId),
                    FOREIGN KEY(collectionId) REFERENCES collection(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(filmId) REFERENCES film_cache(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_collection_film_cross_ref_filmId ON collection_film_cross_ref(filmId)"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Person table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS person (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    age TEXT NOT NULL,
                    eyeColor TEXT NOT NULL,
                    hairColor TEXT NOT NULL,
                    films TEXT NOT NULL,
                    species TEXT NOT NULL,
                    url TEXT NOT NULL
                )
                """.trimIndent()
            )
            
            // Location table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS location (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    climate TEXT NOT NULL,
                    terrain TEXT NOT NULL,
                    surfaceWater TEXT NOT NULL,
                    residents TEXT NOT NULL,
                    films TEXT NOT NULL,
                    url TEXT NOT NULL
                )
                """.trimIndent()
            )
            
            // Species table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS species (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    classification TEXT NOT NULL,
                    eyeColors TEXT NOT NULL,
                    hairColors TEXT NOT NULL,
                    people TEXT NOT NULL,
                    films TEXT NOT NULL,
                    url TEXT NOT NULL
                )
                """.trimIndent()
            )
            
            // Vehicle table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS vehicle (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    vehicleClass TEXT NOT NULL,
                    length TEXT NOT NULL,
                    pilot TEXT NOT NULL,
                    films TEXT NOT NULL,
                    url TEXT NOT NULL
                )
                """.trimIndent()
            )
            
            // Cross-reference tables
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS film_person_cross_ref (
                    filmId TEXT NOT NULL,
                    personId TEXT NOT NULL,
                    PRIMARY KEY(filmId, personId),
                    FOREIGN KEY(filmId) REFERENCES film_cache(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(personId) REFERENCES person(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_film_person_cross_ref_personId ON film_person_cross_ref(personId)")
            
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS film_location_cross_ref (
                    filmId TEXT NOT NULL,
                    locationId TEXT NOT NULL,
                    PRIMARY KEY(filmId, locationId),
                    FOREIGN KEY(filmId) REFERENCES film_cache(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(locationId) REFERENCES location(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_film_location_cross_ref_locationId ON film_location_cross_ref(locationId)")
            
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS film_species_cross_ref (
                    filmId TEXT NOT NULL,
                    speciesId TEXT NOT NULL,
                    PRIMARY KEY(filmId, speciesId),
                    FOREIGN KEY(filmId) REFERENCES film_cache(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(speciesId) REFERENCES species(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_film_species_cross_ref_speciesId ON film_species_cross_ref(speciesId)")
            
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS film_vehicle_cross_ref (
                    filmId TEXT NOT NULL,
                    vehicleId TEXT NOT NULL,
                    PRIMARY KEY(filmId, vehicleId),
                    FOREIGN KEY(filmId) REFERENCES film_cache(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(vehicleId) REFERENCES vehicle(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_film_vehicle_cross_ref_vehicleId ON film_vehicle_cross_ref(vehicleId)")
            
            // Pinned entity table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pinned_entity (
                    entityType TEXT NOT NULL,
                    entityId TEXT NOT NULL,
                    note TEXT,
                    pinnedAt INTEGER NOT NULL,
                    PRIMARY KEY(entityType, entityId)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS recent_view (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    filmId TEXT NOT NULL,
                    openedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
}
