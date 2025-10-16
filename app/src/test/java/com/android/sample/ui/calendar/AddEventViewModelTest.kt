package com.android.sample.ui.calendar

import com.android.sample.model.calendar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Unit tests for AddEventViewModel.
 *
 * Uses a test dispatcher to control coroutine execution for deterministic testing.
 */
class AddEventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: EventRepository
    private lateinit var viewModel: AddEventViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = EventRepositoryLocal()
        viewModel = AddEventViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial UI state has default values`() {
        val state = viewModel.uiState.value
        assertTrue(state.title.isEmpty())
        assertTrue(state.description.isEmpty())
        assertEquals(LocalDate.now(), state.startDate)
        assertEquals(0, state.participants.size)
        assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
    }

    @Test
    fun `setTitle updates the title in UI state`() {
        viewModel.setTitle("SwEnt Meeting")
        assertEquals("SwEnt Meeting", viewModel.uiState.value.title)
    }

    @Test
    fun `setDescription updates the description in UI state`() {
        viewModel.setDescription("Standup meeting")
        assertEquals("Standup meeting", viewModel.uiState.value.description)
    }

    @Test
    fun `addParticipant and removeParticipant modify participants set`() {
        viewModel.addParticipant("user1")
        assertTrue(viewModel.uiState.value.participants.contains("user1"))

        viewModel.removeParticipant("user1")
        assertFalse(viewModel.uiState.value.participants.contains("user1"))
    }

    @Test
    fun `allFieldsValid returns false if title or description is blank`() {
        viewModel.setTitle("")
        viewModel.setDescription("desc")
        assertFalse(viewModel.allFieldsValid())

        viewModel.setTitle("Title")
        viewModel.setDescription("")
        assertFalse(viewModel.allFieldsValid())

        viewModel.setTitle("Title")
        viewModel.setDescription("Desc")
        assertTrue(viewModel.allFieldsValid())
    }

    @Test
    fun `addEvent inserts event into repository`() = runTest {
        viewModel.setTitle("Meeting")
        viewModel.setDescription("Team sync")
        viewModel.setStartHour(10)
        viewModel.setStartMinute(0)
        viewModel.setEndHour(11)
        viewModel.setEndMinute(0)

        viewModel.addEvent()
        testDispatcher.scheduler.advanceUntilIdle() // run pending coroutines

        val events = repository.getAllEvents()
        assertTrue(events.any { it.title == "Meeting" && it.description == "Team sync" })
    }
}
