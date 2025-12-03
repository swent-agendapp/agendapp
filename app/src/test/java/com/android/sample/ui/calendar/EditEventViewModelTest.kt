package com.android.sample.ui.calendar

import androidx.compose.ui.graphics.Color
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryInMemory
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.editEvent.EditEventStep
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditEventViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: EventRepository

  private val selectedOrganizationID: String = "org123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    repository = EventRepositoryInMemory()

    // Set the selected organization in the provider
    // SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial UI state has default values`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
    assertEquals(EditEventStep.MAIN, state.step)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `setTitle updates the title in UI state`() {
    val vm = makeVm()

    vm.setTitle("Updated title")

    assertEquals("Updated title", vm.uiState.value.title)
  }

  @Test
  fun `setDescription updates the description in UI state`() {
    val vm = makeVm()

    vm.setDescription("Updated description")

    assertEquals("Updated description", vm.uiState.value.description)
  }

  @Test
  fun `setColor updates the color in UI state`() {
    val vm = makeVm()
    val newColor = Color(0xFFFF0000)

    vm.setColor(newColor)

    assertEquals(newColor, vm.uiState.value.color)
  }

  @Test
  fun `setStartInstant updates the start instant in UI state`() {
    val vm = makeVm()
    val newStart = Instant.parse("2025-03-01T10:00:00Z")

    vm.setStartInstant(newStart)

    assertEquals(newStart, vm.uiState.value.startInstant)
  }

  @Test
  fun `setEndInstant updates the end instant in UI state`() {
    val vm = makeVm()
    val newEnd = Instant.parse("2025-03-01T11:00:00Z")

    vm.setEndInstant(newEnd)

    assertEquals(newEnd, vm.uiState.value.endInstant)
  }

  @Test
  fun `setRecurrenceMode updates the recurrence mode in UI state`() {
    val vm = makeVm()

    vm.setRecurrenceMode(RecurrenceStatus.Weekly)

    assertEquals(RecurrenceStatus.Weekly, vm.uiState.value.recurrenceMode)
  }

  @Test
  fun `addParticipant and removeParticipant modify participants set`() {
    val vm = makeVm()

    vm.addParticipant("user1")
    assertTrue(vm.uiState.value.participants.contains("user1"))

    vm.removeParticipant("user1")
    assertFalse(vm.uiState.value.participants.contains("user1"))
  }

  @Test
  fun `allFieldsValid returns false if title or description is blank or start after end`() {
    val vm = makeVm()

    // Title blank
    vm.setTitle("")
    vm.setDescription("Desc")
    vm.setStartInstant(Instant.parse("2025-03-01T10:00:00Z"))
    vm.setEndInstant(Instant.parse("2025-03-01T11:00:00Z"))
    assertFalse(vm.allFieldsValid())

    // Description blank
    vm.setTitle("Title")
    vm.setDescription("")
    assertFalse(vm.allFieldsValid())

    // Start after end
    val now = Instant.now()
    vm.setTitle("Title")
    vm.setDescription("Desc")
    vm.setStartInstant(now.plus(Duration.ofHours(2)))
    vm.setEndInstant(now.plus(Duration.ofHours(1)))
    assertFalse(vm.allFieldsValid())

    // All valid
    vm.setStartInstant(now)
    vm.setEndInstant(now.plus(Duration.ofHours(1)))
    assertTrue(vm.allFieldsValid())
  }

  @Test
  fun `goToAttendeesStep and goBackToMainStep update step`() {
    val vm = makeVm()

    assertEquals(EditEventStep.MAIN, vm.uiState.value.step)

    vm.goToAttendeesStep()
    assertEquals(EditEventStep.ATTENDEES, vm.uiState.value.step)

    vm.goBackToMainStep()
    assertEquals(EditEventStep.MAIN, vm.uiState.value.step)
  }

  @Test
  fun `setEditStep updates step in UI state`() {
    val vm = makeVm()

    vm.setEditStep(EditEventStep.ATTENDEES)

    assertEquals(EditEventStep.ATTENDEES, vm.uiState.value.step)
  }

  @Test
  fun `resetUiState sets step back to MAIN`() {
    val vm = makeVm()

    vm.setEditStep(EditEventStep.ATTENDEES)
    assertEquals(EditEventStep.ATTENDEES, vm.uiState.value.step)

    vm.resetUiState()

    assertEquals(EditEventStep.MAIN, vm.uiState.value.step)
  }

  @Test
  fun `loadEvent populates UI state from repository`() = runTest {
    val vm = makeVm()
    val eventId = "event-1"
    val start = Instant.parse("2025-03-01T10:00:00Z")
    val end = Instant.parse("2025-03-01T11:00:00Z")

    val event =
        Event(
            id = eventId,
            organizationId = selectedOrganizationID,
            title = "Original title",
            description = "Original description",
            startDate = start,
            endDate = end,
            cloudStorageStatuses = emptySet(),
            locallyStoredBy = emptyList(),
            personalNotes = null,
            participants = setOf("user1"),
            version = 1L,
            recurrenceStatus = RecurrenceStatus.Weekly,
            hasBeenDeleted = false,
            color = Color(0xFF00FF00))

    repository.insertEvent(orgId = selectedOrganizationID, item = event)

    vm.loadEvent(eventId)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(eventId, state.eventId)
    assertEquals("Original title", state.title)
    assertEquals("Original description", state.description)
    assertEquals(start, state.startInstant)
    assertEquals(end, state.endInstant)
    assertEquals(RecurrenceStatus.Weekly, state.recurrenceMode)
    assertEquals(setOf("user1"), state.participants)
    assertEquals(Color(0xFF00FF00), state.color)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `loadEvent sets errorMessage when event not found`() = runTest {
    val vm = makeVm()

    vm.loadEvent("unknown-id")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Event not found.", state.errorMessage)
  }

  @Test
  fun `saveEditEventChanges updates existing event in repository`() = runTest {
    val vm = makeVm()
    val eventId = "event-2"
    val start = Instant.parse("2025-03-01T10:00:00Z")
    val end = Instant.parse("2025-03-01T11:00:00Z")

    val originalEvent =
        Event(
            id = eventId,
            organizationId = selectedOrganizationID,
            title = "Old title",
            description = "Old description",
            startDate = start,
            endDate = end,
            cloudStorageStatuses = emptySet(),
            locallyStoredBy = emptyList(),
            personalNotes = null,
            participants = setOf("user1"),
            version = 1L,
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            color = Color(0xFF0000FF))

    repository.insertEvent(orgId = selectedOrganizationID, item = originalEvent)

    vm.loadEvent(eventId)
    testDispatcher.scheduler.advanceUntilIdle()

    vm.setTitle("New title")
    vm.setDescription("New description")
    vm.setRecurrenceMode(RecurrenceStatus.Weekly)
    vm.setColor(Color(0xFFFF0000))

    vm.saveEditEventChanges()
    testDispatcher.scheduler.advanceUntilIdle()

    val updated =
        repository.getEventById(orgId = selectedOrganizationID, itemId = eventId)
            ?: error("Event should exist")

    assertEquals("New title", updated.title)
    assertEquals("New description", updated.description)
    assertEquals(RecurrenceStatus.Weekly, updated.recurrenceStatus)
    assertEquals(Color(0xFFFF0000), updated.color)
  }

  /* Helper functions */

  private fun makeVm(): EditEventViewModel {
    return EditEventViewModel(repository = repository)
  }
}
