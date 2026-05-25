package com.example.third_dz.work

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.third_dz.data.backup.BackupRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BackupUserDataWorkerTest {

    @Test
    fun backup_createsFile_returnsSuccess() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val backupRepo = mockk<BackupRepository>(relaxed = true)
        coEvery { backupRepo.export() } returns java.io.File("backup.json")

        val worker = TestListenableWorkerBuilder<BackupUserDataWorker>(ctx)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: android.content.Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    if (workerClassName == BackupUserDataWorker::class.java.name) {
                        return BackupUserDataWorker(appContext, workerParameters, backupRepo)
                    }
                    return null
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun backup_failure_returnsRetryOnFirstAttempt() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val backupRepo = mockk<BackupRepository>(relaxed = true)
        coEvery { backupRepo.export() } throws IOException("Storage full")

        val worker = TestListenableWorkerBuilder<BackupUserDataWorker>(ctx)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: android.content.Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    if (workerClassName == BackupUserDataWorker::class.java.name) {
                        return BackupUserDataWorker(appContext, workerParameters, backupRepo)
                    }
                    return null
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
