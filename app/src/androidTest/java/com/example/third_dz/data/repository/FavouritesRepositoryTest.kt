package com.example.third_dz.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.data.local.AppDatabase
import com.example.third_dz.data.model.Film
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouritesRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: FavouritesRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = FavouritesRepository(database.favouriteFilmDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun makeFilm(id: String = "1") = Film(
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
        people = emptyList(),
        species = emptyList(),
        locations = emptyList(),
        vehicles = emptyList(),
        url = "https://ghibliapi.vercel.app/films/$id"
    )

    // Полный round-trip: Film из API конвертируется, сохраняется и читается без потерь полей
    @Test
    fun addFavourite_filmIsStoredAndRetrievedCorrectly() = runTest {
        val film = makeFilm("1")

        repository.addFavourite(film)

        val result = repository.getAllFavourites().first()
        assertEquals(1, result.size)
        val retrieved = result[0]
        assertEquals(film.id, retrieved.id)
        assertEquals(film.title, retrieved.title)
        assertEquals(film.director, retrieved.director)
        assertEquals(film.release_date, retrieved.release_date)
        assertEquals(film.rt_score, retrieved.rt_score)
    }

    // Нетривиальный: повторное добавление того же фильма — не дубль (бизнес-контракт)
    @Test
    fun addFavourite_calledTwiceWithSameFilm_doesNotCreateDuplicate() = runTest {
        val film = makeFilm("1")

        repository.addFavourite(film)
        repository.addFavourite(film)

        val result = repository.getAllFavourites().first()
        assertEquals(1, result.size)
    }

    @Test
    fun removeFavourite_filmDisappearsFromList() = runTest {
        val film = makeFilm("1")
        repository.addFavourite(film)

        repository.removeFavourite(film.id)

        val result = repository.getAllFavourites().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun isFavourite_returnsTrueAfterAdding() = runTest {
        val film = makeFilm("1")

        repository.addFavourite(film)

        assertTrue(repository.isFavourite(film.id))
    }

    @Test
    fun isFavourite_returnsFalseAfterRemoving() = runTest {
        val film = makeFilm("1")
        repository.addFavourite(film)

        repository.removeFavourite(film.id)

        assertFalse(repository.isFavourite(film.id))
    }

    // Нетривиальный: несколько фильмов — удаление одного не трогает остальных
    @Test
    fun removeFavourite_onlyTargetFilmIsRemoved() = runTest {
        repository.addFavourite(makeFilm("1"))
        repository.addFavourite(makeFilm("2"))
        repository.addFavourite(makeFilm("3"))

        repository.removeFavourite("2")

        val ids = repository.getAllFavourites().first().map { it.id }
        assertEquals(listOf("1", "3"), ids)
    }
}
