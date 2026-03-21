package com.example.third_dz.data.repository

import com.example.third_dz.data.api.GhibliFilmsApi
import com.example.third_dz.data.local.toFavouriteFilmEntity
import com.example.third_dz.data.model.Film
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GhibliFilmsRepositoryTest {

    private val mockApi = mockk<GhibliFilmsApi>()
    private val repository = GhibliFilmsRepository(mockApi)

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

    // Test 8: второй вызов getAllFilms() без forceRefresh не идёт в сеть
    @Test
    fun getAllFilms_cachesResult_doesNotCallApiSecondTime() = runTest {
        val films = listOf(makeFilm("1"), makeFilm("2"))
        coEvery { mockApi.getAllFilms(any(), any()) } returns films

        repository.getAllFilms()
        repository.getAllFilms()

        coVerify(exactly = 1) { mockApi.getAllFilms(any(), any()) }
    }

    // Test 9: маппинг Film → FavouriteFilmEntity сохраняет все поля без потерь
    @Test
    fun film_toFavouriteFilmEntity_mapsAllFieldsCorrectly() {
        val film = makeFilm("42")

        val entity = film.toFavouriteFilmEntity()

        assertEquals(film.id, entity.id)
        assertEquals(film.title, entity.title)
        assertEquals(film.original_title, entity.original_title)
        assertEquals(film.original_title_romanised, entity.original_title_romanised)
        assertEquals(film.description, entity.description)
        assertEquals(film.director, entity.director)
        assertEquals(film.producer, entity.producer)
        assertEquals(film.release_date, entity.release_date)
        assertEquals(film.running_time, entity.running_time)
        assertEquals(film.rt_score, entity.rt_score)
        assertEquals(film.url, entity.url)
    }
}
