package com.example.third_dz.domain.usecase.sync

import app.cash.turbine.test
import com.example.third_dz.data.preferences.SettingsDataStore
import com.example.third_dz.data.repository.GhibliFilmsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IsCatalogueStaleUseCaseTest {

    private val mockRepository = mockk<GhibliFilmsRepository>(relaxed = true)
    private val mockSettings = mockk<SettingsDataStore>(relaxed = true)

    @Test
    fun stale_whenTTLexpired() = runTest {
        every { mockSettings.cacheTtlMinutesFlow } returns flowOf(60L)
        every { mockRepository.isStale(60L) } returns flowOf(true)

        val useCase = IsCatalogueStaleUseCase(mockRepository, mockSettings)

        useCase().test {
            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fresh_whenWithinTTL() = runTest {
        every { mockSettings.cacheTtlMinutesFlow } returns flowOf(60L)
        every { mockRepository.isStale(60L) } returns flowOf(false)

        val useCase = IsCatalogueStaleUseCase(mockRepository, mockSettings)

        useCase().test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stale_whenNeverFetched() = runTest {
        every { mockSettings.cacheTtlMinutesFlow } returns flowOf(60L)
        every { mockRepository.isStale(60L) } returns flowOf(true)

        val useCase = IsCatalogueStaleUseCase(mockRepository, mockSettings)

        useCase().test {
            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
