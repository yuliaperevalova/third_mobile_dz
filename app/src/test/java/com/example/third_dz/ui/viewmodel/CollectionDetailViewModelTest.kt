package com.example.third_dz.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.third_dz.data.local.CollectionEntity
import com.example.third_dz.data.repository.CollectionRepository
import com.example.third_dz.domain.usecase.collection.DeleteCollectionUseCase
import com.example.third_dz.domain.usecase.collection.ObserveFilmsInCollectionUseCase
import com.example.third_dz.domain.usecase.collection.RemoveFilmFromCollectionUseCase
import com.example.third_dz.domain.usecase.collection.RenameCollectionUseCase
import com.example.third_dz.util.makeFilm
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val collectionRepository = mockk<CollectionRepository>()
    private val observeFilms = mockk<ObserveFilmsInCollectionUseCase>()
    private val renameCollection = mockk<RenameCollectionUseCase>(relaxed = true)
    private val deleteCollection = mockk<DeleteCollectionUseCase>(relaxed = true)
    private val removeFilm = mockk<RemoveFilmFromCollectionUseCase>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun vm(collectionId: Long = 5L): CollectionDetailViewModel {
        val handle = SavedStateHandle(mapOf("collectionId" to collectionId))
        return CollectionDetailViewModel(
            savedStateHandle = handle,
            collectionRepository = collectionRepository,
            observeFilms = observeFilms,
            renameCollection = renameCollection,
            deleteCollection = deleteCollection,
            removeFilm = removeFilm
        )
    }

    @Test
    fun collectionFlow_emitsRepositoryValue() = runTest(testDispatcher) {
        val collection = CollectionEntity(id = 5L, name = "X", colorHex = "#FF0000", createdAt = 0L)
        every { collectionRepository.observeCollection(5L) } returns flowOf(collection)
        every { observeFilms(5L) } returns flowOf(emptyList())

        val viewModel = vm()
        viewModel.collection.test {
            assertEquals(null, awaitItem())
            assertEquals(collection, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filmsFlow_emitsUseCaseValue() = runTest(testDispatcher) {
        every { collectionRepository.observeCollection(5L) } returns flowOf(null)
        val film = makeFilm("f1")
        every { observeFilms(5L) } returns MutableStateFlow(listOf(film))

        val viewModel = vm()
        viewModel.films.test {
            assertEquals(emptyList<Any>(), awaitItem())
            assertEquals(listOf(film), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_callsUseCaseWithCollectionId() = runTest(testDispatcher) {
        every { collectionRepository.observeCollection(5L) } returns flowOf(null)
        every { observeFilms(5L) } returns flowOf(emptyList())

        val viewModel = vm()
        viewModel.delete()
        advanceUntilIdle()

        coVerify { deleteCollection(5L) }
    }

    @Test
    fun rename_callsUseCaseWithCollectionIdAndName() = runTest(testDispatcher) {
        every { collectionRepository.observeCollection(5L) } returns flowOf(null)
        every { observeFilms(5L) } returns flowOf(emptyList())

        val viewModel = vm()
        viewModel.rename("New name")
        advanceUntilIdle()

        coVerify { renameCollection(5L, "New name") }
    }
}
