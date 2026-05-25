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
class PinnedEntityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: PinnedEntityDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.pinnedEntityDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun pin_andObserve_returnsEntity() = runTest {
        val entity = PinnedEntity(PinnedType.PERSON, "123", "Test note", 1000L)

        dao.pin(entity)
        val result = dao.observeByType(PinnedType.PERSON).first()

        assertEquals(1, result.size)
        assertEquals("123", result[0].entityId)
        assertEquals("Test note", result[0].note)
    }

    @Test
    fun unpin_removesEntity() = runTest {
        val entity = PinnedEntity(PinnedType.LOCATION, "456", null, 2000L)

        dao.pin(entity)
        dao.unpin(PinnedType.LOCATION, "456")
        val result = dao.observeByType(PinnedType.LOCATION).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeAll_returnsAllTypes() = runTest {
        dao.pin(PinnedEntity(PinnedType.PERSON, "1", null, 1000L))
        dao.pin(PinnedEntity(PinnedType.LOCATION, "2", null, 2000L))
        dao.pin(PinnedEntity(PinnedType.SPECIES, "3", null, 3000L))

        val result = dao.observeAll().first()

        assertEquals(3, result.size)
    }

    @Test
    fun pin_replacesExisting() = runTest {
        dao.pin(PinnedEntity(PinnedType.PERSON, "1", "Old note", 1000L))
        dao.pin(PinnedEntity(PinnedType.PERSON, "1", "New note", 2000L))

        val result = dao.observeByType(PinnedType.PERSON).first()

        assertEquals(1, result.size)
        assertEquals("New note", result[0].note)
        assertEquals(2000L, result[0].pinnedAt)
    }
}
