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
class FavouriteFilmDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavouriteFilmDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.favouriteFilmDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun makeEntity(id: String = "1") = FavouriteFilmEntity(
        id = id,
        title = "Spirited Away",
        original_title = "千と千尋の神隠し",
        original_title_romanised = "Sen to Chihiro no Kamikakushi",
        description = "A girl enters a spirit world",
        director = "Hayao Miyazaki",
        producer = "Toshio Suzuki",
        release_date = "2001",
        running_time = "125",
        rt_score = "97",
        url = "https://ghibliapi.vercel.app/films/$id"
    )

    // Test 1: вставить фильм и прочитать — все поля совпадают
    @Test
    fun insert_andGetAll_returnsInsertedFilm() = runTest {
        val entity = makeEntity("1")
        dao.insert(entity)

        val result = dao.getAllFavourites().first()

        assertEquals(1, result.size)
        assertEquals(entity, result[0])
    }

    // Test 2 (нетривиальный): повторная вставка того же id не создаёт дубль (OnConflictStrategy.REPLACE)
    @Test
    fun insert_duplicate_doesNotCreateDuplicate() = runTest {
        val entity = makeEntity("1")
        dao.insert(entity)
        dao.insert(entity.copy(title = "Updated Title"))

        val result = dao.getAllFavourites().first()

        assertEquals(1, result.size)
        assertEquals("Updated Title", result[0].title)
    }

    // Test 3: вставить и удалить — список пуст
    @Test
    fun delete_removesFilmFromFavourites() = runTest {
        dao.insert(makeEntity("1"))
        dao.delete("1")

        val result = dao.getAllFavourites().first()

        assertTrue(result.isEmpty())
    }
}
