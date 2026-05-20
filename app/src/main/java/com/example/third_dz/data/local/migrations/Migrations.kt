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

object Migrations {
    val ALL: Array<Migration> = arrayOf(MIGRATION_2_3)
}
