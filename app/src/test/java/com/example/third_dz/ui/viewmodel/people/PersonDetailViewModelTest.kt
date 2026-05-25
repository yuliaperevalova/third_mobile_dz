package com.example.third_dz.ui.viewmodel.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.model.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

interface PersonRepository {
    fun observePersonById(id: String): kotlinx.coroutines.flow.Flow<Person?>
}

interface PinnedOperations {
    suspend fun togglePin(type: PinnedType, id: String, note: String?)
}

class TestableObservePersonDetailUseCase(
    private val repo: PersonRepository
) {
    operator fun invoke(personId: String) = repo.observePersonById(personId)
}

class TestableTogglePinUseCase(
    private val ops: PinnedOperations
) {
    suspend operator fun invoke(type: PinnedType, id: String, note: String?) {
        ops.togglePin(type, id, note)
    }
}

class TestablePersonDetailViewModel(
    private val observePersonDetail: TestableObservePersonDetailUseCase,
    private val togglePin: TestableTogglePinUseCase
) : ViewModel() {

    private var currentPersonId: String? = null

    fun loadPerson(personId: String): StateFlow<Person?> {
        currentPersonId = personId
        return observePersonDetail(personId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun onTogglePin(note: String? = null) {
        currentPersonId?.let { id ->
            viewModelScope.launch {
                togglePin(PinnedType.PERSON, id, note)
            }
        }
    }
}

class StubPersonRepository : PersonRepository {
    private val _person = MutableStateFlow<Person?>(null)
    override fun observePersonById(id: String) = _person
    fun setPerson(p: Person?) { _person.value = p }
}

class StubPinnedOps : PinnedOperations {
    var lastCall: Triple<PinnedType, String, String?>? = null
    override suspend fun togglePin(type: PinnedType, id: String, note: String?) {
        lastCall = Triple(type, id, note)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PersonDetailViewModelTest {

    private val stubRepo = StubPersonRepository()
    private val stubOps = StubPinnedOps()

    private val observePersonDetail = TestableObservePersonDetailUseCase(stubRepo)
    private val togglePin = TestableTogglePinUseCase(stubOps)

    private lateinit var viewModel: TestablePersonDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TestablePersonDetailViewModel(observePersonDetail, togglePin)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPerson emits person from use case`() = runTest(testDispatcher) {
        val person = Person(
            id = "1", name = "Ashitaka", gender = "Male", age = "older",
            eye_color = "brown", hair_color = "black",
            films = emptyList(), species = "Human", url = "https://example.com/1"
        )
        stubRepo.setPerson(person)

        val flow = viewModel.loadPerson("1")

        flow.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            val emittedPerson = awaitItem()
            assertNotNull(emittedPerson)
            assertEquals("Ashitaka", emittedPerson?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTogglePin calls toggle pin use case`() = runTest(testDispatcher) {
        viewModel.loadPerson("1")
        viewModel.onTogglePin(note = "Favorite character")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PinnedType.PERSON, stubOps.lastCall?.first)
        assertEquals("1", stubOps.lastCall?.second)
        assertEquals("Favorite character", stubOps.lastCall?.third)
    }
}
