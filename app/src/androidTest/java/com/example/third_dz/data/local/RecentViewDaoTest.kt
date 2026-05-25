package com.example.third_dz.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentViewDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: RecentViewDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.recentViewDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndObserve_returnsEntity() = runTest {
        val entity = RecentViewEntity(filmId = "f1", openedAt = 1000L)
        dao.insert(entity)

        val result = dao.observeRecent(10).first()

        assertEquals(1, result.size)
        assertEquals("f1", result[0].filmId)
    }

    @Test
    fun findRecentByFilmId_returnsWithinWindow() = runTest {
        dao.insert(RecentViewEntity(filmId = "f1", openedAt = 10_000L))

        val result = dao.findRecentByFilmId("f1", since = 5_000L)

        assertEquals(10_000L, result?.openedAt)
    }

    @Test
    fun findRecentByFilmId_returnsNullOutsideWindow() = runTest {
        dao.insert(RecentViewEntity(filmId = "f1", openedAt = 10_000L))

        val result = dao.findRecentByFilmId("f1", since = 20_000L)

        assertEquals(null, result)
    }

    @Test
    fun observeRecent_respectsLimit() = runTest {
        dao.insert(RecentViewEntity(filmId = "f1", openedAt = 1000L))
        dao.insert(RecentViewEntity(filmId = "f2", openedAt = 2000L))
        dao.insert(RecentViewEntity(filmId = "f3", openedAt = 3000L))

        val result = dao.observeRecent(2).first()

        assertEquals(2, result.size)
    }

    @Test
    fun clear_removesAllEntries() = runTest {
        dao.insert(RecentViewEntity(filmId = "f1", openedAt = 1000L))
        dao.insert(RecentViewEntity(filmId = "f2", openedAt = 2000L))
        dao.clear()

        val result = dao.observeRecent(10).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteById_removesSpecificEntry() = runTest {
        val id = dao.insert(RecentViewEntity(filmId = "f1", openedAt = 1000L))
        dao.insert(RecentViewEntity(filmId = "f2", openedAt = 2000L))
        dao.deleteById(id)

        val result = dao.observeRecent(10).first()

        assertEquals(1, result.size)
        assertEquals("f2", result[0].filmId)
    }
}
