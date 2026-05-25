package com.example.third_dz.util

import com.example.third_dz.data.model.Film

fun makeFilm(id: String = "1") = Film(
    id = id,
    title = "Spirited Away",
    originalTitle = "千と千尋の神隠し",
    originalTitleRomanised = "Sen to Chihiro no Kamikakushi",
    description = "A girl enters a spirit world",
    director = "Hayao Miyazaki",
    producer = "Toshio Suzuki",
    releaseDate = "2001",
    runningTime = "125",
    rtScore = "97",
    people = emptyList(),
    species = emptyList(),
    locations = emptyList(),
    vehicles = emptyList(),
    url = "https://ghibliapi.vercel.app/films/$id"
)
