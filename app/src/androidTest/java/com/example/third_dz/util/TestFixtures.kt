package com.example.third_dz.util

import com.example.third_dz.data.model.Film

fun makeFilm(id: String = "1") = Film(
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
