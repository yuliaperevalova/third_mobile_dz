package com.example.third_dz.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniverseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var peopleDao: PeopleDao
    private lateinit var locationsDao: LocationsDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        peopleDao = database.peopleDao()
        locationsDao = database.locationsDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun batchInsert_people_insertsAll() = runTest {
        val people = listOf(
            PersonEntity("1", "Ashitaka", "Male", "17", "Brown", "Brown", emptyList(), "", "url1"),
            PersonEntity("2", "San", "Female", "17", "Brown", "Black", emptyList(), "", "url2")
        )

        peopleDao.insertAll(people)
        val result = peopleDao.getAll()

        assertEquals(2, result.size)
        assertEquals("Ashitaka", result[0].name)
        assertEquals("San", result[1].name)
    }

    @Test
    fun batchInsert_locations_insertsAll() = runTest {
        val locations = listOf(
            LocationEntity("1", "Iron Town", "Continental", "Mountain", "40", emptyList(), emptyList(), "url1"),
            LocationEntity("2", "Forest", "Temperate", "Forest", "70", emptyList(), emptyList(), "url2")
        )

        locationsDao.insertAll(locations)
        val result = locationsDao.getAll()

        assertEquals(2, result.size)
        assertEquals("Iron Town", result[0].name)
    }

    @Test
    fun insertAll_replacesOnConflict() = runTest {
        val person1 = PersonEntity("1", "Old Name", "Male", "17", "Brown", "Brown", emptyList(), "", "url1")
        val person2 = PersonEntity("1", "New Name", "Male", "17", "Brown", "Brown", emptyList(), "", "url1")

        peopleDao.insertAll(listOf(person1))
        peopleDao.insertAll(listOf(person2))
        val result = peopleDao.getAll()

        assertEquals(1, result.size)
        assertEquals("New Name", result[0].name)
    }
}
