package com.example.third_dz.domain.usecase.universe

import com.example.third_dz.data.repository.UniverseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RefreshUniverseUseCaseTest {

    private val repository = mockk<UniverseRepository>()
    private val useCase = RefreshUniverseUseCase(repository)

    @Test
    fun invoke_callsRepositoryRefreshAll() = runTest {
        coEvery { repository.refreshAll() } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.refreshAll() }
    }

    @Test
    fun invoke_propagatesException() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { repository.refreshAll() } throws exception

        try {
            useCase()
            assert(false) { "Should throw exception" }
        } catch (e: RuntimeException) {
            assert(e.message == "Network error")
        }

        coVerify(exactly = 1) { repository.refreshAll() }
    }
}
