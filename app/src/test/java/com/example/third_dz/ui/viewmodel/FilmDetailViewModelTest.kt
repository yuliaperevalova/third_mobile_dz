package com.example.third_dz.ui.viewmodel

import com.example.third_dz.data.local.WatchStatus
import com.example.third_dz.data.repository.GhibliFilmsRepository
import com.example.third_dz.domain.model.UserFilmRecord
import com.example.third_dz.domain.usecase.record.ObserveFilmRecordUseCase
import com.example.third_dz.domain.usecase.record.SetNoteUseCase
import com.example.third_dz.domain.usecase.record.SetRatingUseCase
import com.example.third_dz.domain.usecase.record.SetWatchStatusUseCase
import com.example.third_dz.ui.event.FilmDetailEvent
import com.example.third_dz.ui.state.FilmDetailUiState
import com.example.third_dz.util.makeFilm
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilmDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<GhibliFilmsRepository>()
    private val setStatus = mockk<SetWatchStatusUseCase>(relaxed = true)
    private val setRating = mockk<SetRatingUseCase>(relaxed = true)
    private val setNote = mockk<SetNoteUseCase>(relaxed = true)
    private val observeRecord = mockk<ObserveFilmRecordUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun vm() = FilmDetailViewModel(repository, setStatus, setRating, setNote, observeRecord)

    @Test
    fun loadFilm_success_emitsSuccessWithRecord() = runTest(testDispatcher) {
        val film = makeFilm("f1")
        val record = UserFilmRecord("f1", WatchStatus.WATCHED, 9, null, 500L, 500L)
        coEvery { repository.getFilmById("f1") } returns film
        every { observeRecord("f1") } returns flowOf(record)

        val vm = vm()
        vm.loadFilm("f1")
        advanceUntilIdle()

        val state = vm.uiState
        assertTrue(state is FilmDetailUiState.Success)
        val success = state as FilmDetailUiState.Success
        assertEquals(film, success.film)
        assertEquals(record, success.record)
    }

    @Test
    fun loadFilm_combinesFilmAndNullRecord() = runTest(testDispatcher) {
        val film = makeFilm("f1")
        coEvery { repository.getFilmById("f1") } returns film
        every { observeRecord("f1") } returns flowOf(null)

        val vm = vm()
        vm.loadFilm("f1")
        advanceUntilIdle()

        val success = vm.uiState as FilmDetailUiState.Success
        assertNull(success.record)
    }

    @Test
    fun recordUpdate_emitsNewState() = runTest(testDispatcher) {
        val film = makeFilm("f1")
        val recordFlow = MutableStateFlow<UserFilmRecord?>(null)
        coEvery { repository.getFilmById("f1") } returns film
        every { observeRecord("f1") } returns recordFlow

        val vm = vm()
        vm.loadFilm("f1")
        advanceUntilIdle()
        assertNull((vm.uiState as FilmDetailUiState.Success).record)

        recordFlow.value = UserFilmRecord("f1", WatchStatus.WATCHED, null, null, 999L, 999L)
        advanceUntilIdle()
        assertEquals(WatchStatus.WATCHED, (vm.uiState as FilmDetailUiState.Success).record?.status)
    }

    @Test
    fun loadFilm_error_emitsErrorState() = runTest(testDispatcher) {
        coEvery { repository.getFilmById("f1") } throws RuntimeException("boom")
        every { observeRecord("f1") } returns flowOf(null)

        val vm = vm()
        vm.loadFilm("f1")
        advanceUntilIdle()

        assertTrue(vm.uiState is FilmDetailUiState.Error)
    }

    @Test
    fun setStatusEvent_invokesUseCase() = runTest(testDispatcher) {
        val film = makeFilm("f1")
        coEvery { repository.getFilmById("f1") } returns film
        every { observeRecord("f1") } returns flowOf(null)

        val vm = vm()
        vm.loadFilm("f1")
        advanceUntilIdle()
        vm.onEvent(FilmDetailEvent.SetStatus("f1", WatchStatus.WATCHED))
        advanceUntilIdle()

        coVerify { setStatus("f1", WatchStatus.WATCHED, any()) }
    }
}
