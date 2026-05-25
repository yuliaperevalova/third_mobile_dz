package com.example.third_dz.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.data.repository.UniverseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class InitialPreloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val filmsRepository: GhibliFilmsRepository,
    private val universeRepository: UniverseRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            filmsRepository.refreshFilms()
            universeRepository.refreshAll()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
