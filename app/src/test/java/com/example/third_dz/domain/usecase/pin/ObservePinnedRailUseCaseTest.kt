package com.example.third_dz.domain.usecase.pin

import app.cash.turbine.test
import com.example.third_dz.data.local.PinnedEntity
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.repository.PinnedRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePinnedRailUseCaseTest {

    private val repository = mockk<PinnedRepository>()
    private val useCase = ObservePinnedRailUseCase(repository)

    @Test
    fun invoke_returnsAllPinnedEntities() = runTest {
        val pinned = listOf(
            PinnedEntity(PinnedType.PERSON, "1", null, 1000L),
            PinnedEntity(PinnedType.LOCATION, "2", "Some note", 2000L),
            PinnedEntity(PinnedType.SPECIES, "3", null, 3000L)
        )
        every { repository.observeAll() } returns flowOf(pinned)

        useCase().test {
            assertEquals(pinned, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenEmpty_returnsEmptyList() = runTest {
        every { repository.observeAll() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_withMultipleTypes_returnsAllInSingleList() = runTest {
        val pinned = listOf(
            PinnedEntity(PinnedType.PERSON, "p1", "Character", 100L),
            PinnedEntity(PinnedType.LOCATION, "l1", "Place", 200L),
            PinnedEntity(PinnedType.SPECIES, "s1", null, 300L),
            PinnedEntity(PinnedType.VEHICLE, "v1", "Vehicle", 400L)
        )
        every { repository.observeAll() } returns flowOf(pinned)

        useCase().test {
            val items = awaitItem()
            assertEquals(4, items.size)
            assertEquals(PinnedType.PERSON, items[0].entityType)
            assertEquals(PinnedType.LOCATION, items[1].entityType)
            assertEquals(PinnedType.SPECIES, items[2].entityType)
            assertEquals(PinnedType.VEHICLE, items[3].entityType)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
