package com.example.third_dz.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserFilmRecordDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: UserFilmRecordDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.userFilmRecordDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun rec(
        filmId: String = "f1",
        status: WatchStatus = WatchStatus.PLAN,
        rating: Int? = null,
        note: String? = null,
        watchedAt: Long? = null,
        updatedAt: Long = 100L
    ) = UserFilmRecordEntity(filmId, status, rating, note, watchedAt, updatedAt)

    @Test
    fun upsert_insertsNewRecord() = runTest {
        dao.upsert(rec("f1", WatchStatus.WATCHING))
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals(WatchStatus.WATCHING, all[0].status)
    }

    @Test
    fun upsert_updatesExistingRecord() = runTest {
        dao.upsert(rec("f1", WatchStatus.PLAN))
        dao.upsert(rec("f1", WatchStatus.WATCHED, rating = 8, updatedAt = 200L))
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals(WatchStatus.WATCHED, all[0].status)
        assertEquals(8, all[0].rating)
        assertEquals(200L, all[0].updatedAt)
    }

    @Test
    fun delete_removesRecord() = runTest {
        dao.upsert(rec("f1"))
        dao.upsert(rec("f2"))
        dao.delete("f1")
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("f2", all[0].filmId)
    }

    @Test
    fun observeByFilm_returnsNullWhenAbsent() = runTest {
        assertNull(dao.observeByFilm("missing").first())
    }

    @Test
    fun observeByFilm_returnsRecordWhenPresent() = runTest {
        dao.upsert(rec("f1", WatchStatus.DROPPED))
        val record = dao.observeByFilm("f1").first()
        assertEquals(WatchStatus.DROPPED, record?.status)
    }
}
