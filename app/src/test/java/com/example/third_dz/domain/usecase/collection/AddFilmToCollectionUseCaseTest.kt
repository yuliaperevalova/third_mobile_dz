package com.example.third_dz.domain.usecase.collection

import com.example.third_dz.data.repository.CollectionRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddFilmToCollectionUseCaseTest {

    private val repository = mockk<CollectionRepository>(relaxed = true)
    private val useCase = AddFilmToCollectionUseCase(repository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        useCase(7L, "f1")
        coVerify { repository.addFilm(7L, "f1") }
    }

    @Test
    fun invokeTwice_callsRepositoryTwice() = runTest {
        useCase(7L, "f1")
        useCase(7L, "f1")
        coVerify(exactly = 2) { repository.addFilm(7L, "f1") }
    }
}
