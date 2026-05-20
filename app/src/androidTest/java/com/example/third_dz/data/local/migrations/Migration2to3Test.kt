package com.example.third_dz.data.local.migrations

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.local.AppDatabase
import com.example.third_dz.data.local.WatchStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2to3Test {

    private val ctx = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "migration_test.db"

    @Before
    fun reset() {
        ctx.deleteDatabase(dbName)
    }

    @After
    fun teardown() {
        ctx.deleteDatabase(dbName)
    }

    private fun createV2Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(ctx)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE favourite_films (
                                id TEXT NOT NULL PRIMARY KEY,
                                title TEXT NOT NULL,
                                original_title TEXT NOT NULL,
                                original_title_romanised TEXT NOT NULL,
                                description TEXT NOT NULL,
                                director TEXT NOT NULL,
                                producer TEXT NOT NULL,
                                release_date TEXT NOT NULL,
                                running_time TEXT NOT NULL,
                                rt_score TEXT NOT NULL,
                                url TEXT NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE film_cache (
                                id TEXT NOT NULL PRIMARY KEY,
                                title TEXT NOT NULL,
                                original_title TEXT NOT NULL,
                                original_title_romanised TEXT NOT NULL,
                                description TEXT NOT NULL,
                                director TEXT NOT NULL,
                                producer TEXT NOT NULL,
                                release_date TEXT NOT NULL,
                                running_time TEXT NOT NULL,
                                rt_score TEXT NOT NULL,
                                url TEXT NOT NULL
                            )
                            """.trimIndent()
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS room_master_table (
                                id INTEGER PRIMARY KEY, identity_hash TEXT
                            )
                            """.trimIndent()
                        )
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val db = helper.writableDatabase
        db.execSQL(
            """
            INSERT INTO favourite_films VALUES(
                'f1','Spirited Away','千と千尋','Sen','desc',
                'Miyazaki','Suzuki','2001','125','97','url'
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO favourite_films VALUES(
                'f2','Totoro','となりのトトロ','Tonari','desc2',
                'Miyazaki','Suzuki','1988','86','93','url2'
            )
            """.trimIndent()
        )
        helper.close()
    }

    @Test
    fun migration_transfersFavouritesToWatchedRecords() = runTest {
        createV2Database()

        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_2_3)
            .build()

        val records = db.userFilmRecordDao().observeAll().first()
        db.close()

        assertEquals(2, records.size)
        val byId = records.associateBy { it.filmId }
        assertEquals(WatchStatus.WATCHED, byId["f1"]?.status)
        assertEquals(WatchStatus.WATCHED, byId["f2"]?.status)
    }
}
