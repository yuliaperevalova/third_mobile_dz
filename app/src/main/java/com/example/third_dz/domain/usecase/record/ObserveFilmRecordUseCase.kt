package com.example.third_dz.domain.usecase.record

import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFilmRecordUseCase @Inject constructor(
    private val repository: UserFilmRecordRepository
) {
    operator fun invoke(filmId: String): Flow<UserFilmRecord?> =
        repository.observeByFilm(filmId)
}
