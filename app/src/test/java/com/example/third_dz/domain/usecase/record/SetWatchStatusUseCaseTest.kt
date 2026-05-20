package com.example.third_dz.domain.usecase.record

import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.repository.UserFilmRecordRepository
import com.example.third_dz.domain.model.UserFilmRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SetWatchStatusUseCaseTest {

    private val repository = mockk<UserFilmRecordRepository>(relaxed = true)
    private val useCase = SetWatchStatusUseCase(repository)

    private val now = 1700_000_000_000L

    @Test
    fun firstTransitionToWatched_setsWatchedAt() = runTest {
        coEvery { repository.observeByFilm("f1") } returns flowOf(null)
        val slot = slot<UserFilmRecord>()
        coEvery { repository.upsert(capture(slot)) } returns Unit

        useCase("f1", WatchStatus.WATCHED, now)

        assertEquals(WatchStatus.WATCHED, slot.captured.status)
        assertEquals(now, slot.captured.watchedAt)
        assertEquals(now, slot.captured.updatedAt)
    }

    @Test
    fun planFromWatched_clearsWatchedAt() = runTest {
        val existing = UserFilmRecord("f1", WatchStatus.WATCHED, null, null, 500L, 500L)
        coEvery { repository.observeByFilm("f1") } returns flowOf(existing)
        val slot = slot<UserFilmRecord>()
        coEvery { repository.upsert(capture(slot)) } returns Unit

        useCase("f1", WatchStatus.PLAN, now)

        assertEquals(WatchStatus.PLAN, slot.captured.status)
        assertNull(slot.captured.watchedAt)
    }

    @Test
    fun watchedToWatched_preservesWatchedAt() = runTest {
        val originalWatchedAt = 500L
        val existing = UserFilmRecord("f1", WatchStatus.WATCHED, null, null, originalWatchedAt, 500L)
        coEvery { repository.observeByFilm("f1") } returns flowOf(existing)
        val slot = slot<UserFilmRecord>()
        coEvery { repository.upsert(capture(slot)) } returns Unit

        useCase("f1", WatchStatus.WATCHED, now)

        assertEquals(originalWatchedAt, slot.captured.watchedAt)
    }

    @Test
    fun planToWatched_setsWatchedAt() = runTest {
        val existing = UserFilmRecord("f1", WatchStatus.PLAN, 7, "note", null, 500L)
        coEvery { repository.observeByFilm("f1") } returns flowOf(existing)
        val slot = slot<UserFilmRecord>()
        coEvery { repository.upsert(capture(slot)) } returns Unit

        useCase("f1", WatchStatus.WATCHED, now)

        assertEquals(now, slot.captured.watchedAt)
        assertEquals(7, slot.captured.rating)
        assertEquals("note", slot.captured.note)
    }

    @Test
    fun statusChange_callsUpsertOnce() = runTest {
        coEvery { repository.observeByFilm("f1") } returns flowOf(null)
        useCase("f1", WatchStatus.WATCHING, now)
        coVerify(exactly = 1) { repository.upsert(any()) }
    }
}
