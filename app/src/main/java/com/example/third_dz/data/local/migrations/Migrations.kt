package com.example.third_dz.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

object Migrations {
    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3, MIGRATION_3_4)
}
