package com.android.sample.ui.calendar

import com.android.sample.model.calendar.*
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

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
  fun `setStartInstant updates the start instant in UI state`() {
    val newStart = Instant.parse("2025-03-01T10:00:00Z")
    viewModel.setStartInstant(newStart)
    assertEquals(newStart, viewModel.uiState.value.startInstant)
  }

  @Test
  fun `setEndInstant updates the end instant in UI state`() {
    val newEnd = Instant.parse("2025-03-01T11:00:00Z")
    viewModel.setEndInstant(newEnd)
    assertEquals(newEnd, viewModel.uiState.value.endInstant)
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
  fun `startTimeIsAfterEndTime returns true if start instant is after end instant`() {
    val now = Instant.now()
    viewModel.setStartInstant(now.plus(Duration.ofHours(2)))
    viewModel.setEndInstant(now.plus(Duration.ofHours(1)))
    assertTrue(viewModel.startTimeIsAfterEndTime())

    viewModel.setStartInstant(now)
    viewModel.setEndInstant(now.plus(Duration.ofHours(1)))
    assertFalse(viewModel.startTimeIsAfterEndTime())
  }

  @Test
  fun `addEvent inserts event into repository`() = runTest {
    viewModel.setTitle("Meeting")
    viewModel.setDescription("Team sync")
    viewModel.setStartInstant(Instant.now())
    viewModel.setEndInstant(Instant.now().plus(Duration.ofHours(1)))

    viewModel.addEvent()
    testDispatcher.scheduler.advanceUntilIdle() // run pending coroutines

    val events = repository.getAllEvents()
    assertTrue(events.any { it.title == "Meeting" && it.description == "Team sync" })
  }

  @Test
  fun `resetUiState clears all fields to default`() {
    viewModel.setTitle("Some Title")
    viewModel.setDescription("Some Description")
    viewModel.addParticipant("user1")
    viewModel.setStartInstant(Instant.parse("2025-03-01T10:00:00Z"))
    viewModel.setEndInstant(Instant.parse("2025-03-01T11:00:00Z"))
    viewModel.setRecurrenceMode(RecurrenceStatus.Weekly)

    viewModel.resetUiState()
    val state = viewModel.uiState.value
    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
  }
}
