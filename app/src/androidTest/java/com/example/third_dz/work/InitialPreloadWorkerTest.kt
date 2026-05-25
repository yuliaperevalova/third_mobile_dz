package com.example.third_dz.work

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.UniverseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class InitialPreloadWorkerTest {

    @Test
    fun networkFailure_returnsRetry() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val filmsRepo = mockk<GhibliFilmsRepository>(relaxed = true)
        val universeRepo = mockk<UniverseRepository>(relaxed = true)
        coEvery { filmsRepo.refreshFilms() } throws IOException("No network")

        val worker = TestListenableWorkerBuilder<InitialPreloadWorker>(ctx)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: android.content.Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    if (workerClassName == InitialPreloadWorker::class.java.name) {
                        return InitialPreloadWorker(appContext, workerParameters, filmsRepo, universeRepo)
                    }
                    return null
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { filmsRepo.refreshFilms() }
    }

    @Test
    fun success_returnsSuccess() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val filmsRepo = mockk<GhibliFilmsRepository>(relaxed = true)
        val universeRepo = mockk<UniverseRepository>(relaxed = true)
        coEvery { filmsRepo.refreshFilms() } returns Unit
        coEvery { universeRepo.refreshAll() } returns Unit

        val worker = TestListenableWorkerBuilder<InitialPreloadWorker>(ctx)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: android.content.Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    if (workerClassName == InitialPreloadWorker::class.java.name) {
                        return InitialPreloadWorker(appContext, workerParameters, filmsRepo, universeRepo)
                    }
                    return null
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { filmsRepo.refreshFilms() }
        coVerify { universeRepo.refreshAll() }
    }

    @Test
    fun maxRetriesExhausted_returnsFailure() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val filmsRepo = mockk<GhibliFilmsRepository>(relaxed = true)
        val universeRepo = mockk<UniverseRepository>(relaxed = true)
        coEvery { filmsRepo.refreshFilms() } throws IOException("No network")

        val worker = TestListenableWorkerBuilder<InitialPreloadWorker>(ctx)
            .setRunAttemptCount(3)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: android.content.Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    if (workerClassName == InitialPreloadWorker::class.java.name) {
                        return InitialPreloadWorker(appContext, workerParameters, filmsRepo, universeRepo)
                    }
                    return null
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }
}
