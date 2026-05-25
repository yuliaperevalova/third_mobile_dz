package com.example.third_dz.data.backup

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.local.*
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.local.CollectionFilmCrossRef
import com.example.third_dz.data.local.PinnedEntity
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.local.RecentViewEntity
import com.example.third_dz.data.local.UserFilmRecordEntity
import com.example.third_dz.data.local.WatchStatus
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var recordDao: UserFilmRecordDao
    private lateinit var collectionDao: CollectionDao
    private lateinit var pinnedDao: PinnedEntityDao
    private lateinit var recentViewDao: RecentViewDao
    private lateinit var repository: BackupRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        recordDao = database.userFilmRecordDao()
        collectionDao = database.collectionDao()
        pinnedDao = database.pinnedEntityDao()
        recentViewDao = database.recentViewDao()

        repository = BackupRepository(
            context = ApplicationProvider.getApplicationContext(),
            gson = GsonBuilder().create(),
            recordDao = recordDao,
            collectionDao = collectionDao,
            pinnedDao = pinnedDao,
            recentViewDao = recentViewDao
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun exportImport_roundTrip_restoresAllData() = runTest {
        recordDao.upsert(UserFilmRecordEntity("f1", WatchStatus.WATCHED, rating = 8, note = null, watchedAt = 1000L, updatedAt = 2000L))
        recordDao.upsert(UserFilmRecordEntity("f2", WatchStatus.PLAN, rating = null, note = null, watchedAt = null, updatedAt = 3000L))

        database.filmDao().insertAll(listOf(
            com.example.third_dz.data.local.FilmEntity(
                id = "f1", title = "Film1", original_title = "", original_title_romanised = "",
                description = "", director = "", producer = "", release_date = "",
                running_time = "", rt_score = "", url = "", lastFetchedAt = 0L
            ),
            com.example.third_dz.data.local.FilmEntity(
                id = "f2", title = "Film2", original_title = "", original_title_romanised = "",
                description = "", director = "", producer = "", release_date = "",
                running_time = "", rt_score = "", url = "", lastFetchedAt = 0L
            )
        ))

        val collId = collectionDao.insert(CollectionEntity(name = "Favourites", colorHex = "#FF0000", createdAt = 1000L))
        collectionDao.addFilm(CollectionFilmCrossRef(collId, filmId = "f1"))

        pinnedDao.pin(PinnedEntity(PinnedType.PERSON, "p1", note = "Hero", pinnedAt = 5000L))

        recentViewDao.insert(RecentViewEntity(filmId = "f1", openedAt = 6000L))

        val file = repository.export()
        assertTrue(file.exists())

        val freshDb = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        freshDb.filmDao().insertAll(listOf(
            com.example.third_dz.data.local.FilmEntity(
                id = "f1", title = "F1", original_title = "", original_title_romanised = "",
                description = "", director = "", producer = "", release_date = "",
                running_time = "", rt_score = "", url = "", lastFetchedAt = 0L
            ),
            com.example.third_dz.data.local.FilmEntity(
                id = "f2", title = "F2", original_title = "", original_title_romanised = "",
                description = "", director = "", producer = "", release_date = "",
                running_time = "", rt_score = "", url = "", lastFetchedAt = 0L
            )
        ))

        val freshRepo = BackupRepository(
            context = ApplicationProvider.getApplicationContext(),
            gson = GsonBuilder().create(),
            recordDao = freshDb.userFilmRecordDao(),
            collectionDao = freshDb.collectionDao(),
            pinnedDao = freshDb.pinnedEntityDao(),
            recentViewDao = freshDb.recentViewDao()
        )

        val result = freshRepo.import(file)
        assertTrue(result.isSuccess)

        val importedRecords = freshDb.userFilmRecordDao().observeAll().first()
        assertEquals(2, importedRecords.size)
        assertEquals(WatchStatus.WATCHED, importedRecords.find { it.filmId == "f1" }?.status)

        val importedCollections = freshDb.collectionDao().observeCollections().first()
        assertEquals(1, importedCollections.size)
        assertEquals("Favourites", importedCollections[0].name)
        val filmsInCol = freshDb.collectionDao().observeFilmsInCollection(importedCollections[0].id).first()
        assertEquals(1, filmsInCol.size)

        val importedPins = freshDb.pinnedEntityDao().observeAll().first()
        assertEquals(1, importedPins.size)
        assertEquals("p1", importedPins[0].entityId)

        val importedRecentViews = freshDb.recentViewDao().observeRecent(10).first()
        assertEquals(1, importedRecentViews.size)

        freshDb.close()
        file.delete()
    }

    @Test
    fun rotation_keepsLast4Files() = runTest {
        val files = (1..5).map { i ->
            val backup = UserDataBackup(
                timestamp = 1000L * i,
                records = emptyList(),
                collections = emptyList(),
                pins = emptyList(),
                recentViews = emptyList()
            )
            val dir = File(ApplicationProvider.getApplicationContext<android.content.Context>().filesDir, "backups")
            dir.mkdirs()
            val file = File(dir, "backup_${1000L * i}.json")
            file.writeText(GsonBuilder().create().toJson(backup))
            file
        }

        val repo = BackupRepository(
            context = ApplicationProvider.getApplicationContext(),
            gson = GsonBuilder().create(),
            recordDao = recordDao,
            collectionDao = collectionDao,
            pinnedDao = pinnedDao,
            recentViewDao = recentViewDao
        )

        repo.export()

        val dir = File(ApplicationProvider.getApplicationContext<android.content.Context>().filesDir, "backups")
        val remaining = dir.listFiles()?.filter { it.name.startsWith("backup_") } ?: emptyList()
        assertTrue("Expected at most 4 backup files, got ${remaining.size}", remaining.size <= 4)

        files.forEach { it.delete() }
    }

    @Test
    fun import_validJson_restoresRecordsAndPins() = runTest {
        val json = """{
            "version": 1,
            "timestamp": 1000,
            "records": [
                {"filmId": "f1", "status": "WATCHED", "rating": 9, "note": "Great", "watchedAt": 100, "updatedAt": 200}
            ],
            "collections": [],
            "pins": [
                {"entityType": "LOCATION", "entityId": "loc1", "note": null, "pinnedAt": 300}
            ],
            "recentViews": []
        }"""

        val file = File.createTempFile("backup_test", ".json", ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir)
        file.writeText(json)

        val result = repository.import(file)
        assertTrue(result.isSuccess)

        val records = recordDao.observeAll().first()
        assertEquals(1, records.size)
        assertEquals(9, records[0].rating)

        val pins = pinnedDao.observeAll().first()
        assertEquals(1, pins.size)
        assertEquals("loc1", pins[0].entityId)

        file.delete()
    }
}
