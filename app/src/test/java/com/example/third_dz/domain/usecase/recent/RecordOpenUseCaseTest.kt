package com.example.third_dz.domain.usecase.recent

import com.example.third_dz.data.repository.RecentViewRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RecordOpenUseCaseTest {

    private val repository = mockk<RecentViewRepository>(relaxed = true)
    private val useCase = RecordOpenUseCase(repository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        useCase("f1")
        coVerify { repository.recordOpen("f1", any()) }
    }

    @Test
    fun invokeMultipleTimes_delegatesEachTime() = runTest {
        useCase("f1")
        useCase("f1")
        coVerify(exactly = 2) { repository.recordOpen("f1", any()) }
    }
}
