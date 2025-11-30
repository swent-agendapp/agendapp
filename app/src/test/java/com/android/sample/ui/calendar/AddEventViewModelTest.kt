package com.android.sample.ui.calendar

import androidx.compose.ui.graphics.Color
import com.android.sample.model.calendar.*
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
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
class AddEventViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: EventRepository

  private val selectedOrganizationID = "org123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    repository = EventRepositoryLocal()

    // Set a selected organization
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ------------------------------------------------------------
  //  UI State tests
  // ------------------------------------------------------------

  @Test
  fun `initial UI state has default values`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
  }

  @Test
  fun `setTitle updates the title in UI state`() {
    val vm = makeVm()
    vm.setTitle("SwEnt Meeting")
    assertEquals("SwEnt Meeting", vm.uiState.value.title)
  }

  @Test
  fun `setColor updates the color in UI state`() {
    val vm = makeVm()
    val newColor = Color(0xFFFF0000)

    vm.setColor(newColor)

    assertEquals(newColor, vm.uiState.value.color)
  }

  @Test
  fun `setDescription updates the description in UI state`() {
    val vm = makeVm()
    vm.setDescription("Standup meeting")
    assertEquals("Standup meeting", vm.uiState.value.description)
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
  fun `addParticipant and removeParticipant modify participants set`() {
    val vm = makeVm()

    vm.addParticipant("user1")
    assertTrue(vm.uiState.value.participants.contains("user1"))

    vm.removeParticipant("user1")
    assertFalse(vm.uiState.value.participants.contains("user1"))
  }

  @Test
  fun `allFieldsValid returns false if title or description is blank`() {
    val vm = makeVm()

    vm.setTitle("")
    vm.setDescription("desc")
    assertFalse(vm.allFieldsValid())

    vm.setTitle("Title")
    vm.setDescription("")
    assertFalse(vm.allFieldsValid())

    vm.setTitle("Title")
    vm.setDescription("Desc")
    assertTrue(vm.allFieldsValid())
  }

  @Test
  fun `startTimeIsAfterEndTime returns true if start instant is after end instant`() {
    val vm = makeVm()
    val now = Instant.now()

    vm.setStartInstant(now.plus(Duration.ofHours(2)))
    vm.setEndInstant(now.plus(Duration.ofHours(1)))
    assertTrue(vm.startTimeIsAfterEndTime())

    vm.setStartInstant(now)
    vm.setEndInstant(now.plus(Duration.ofHours(1)))
    assertFalse(vm.startTimeIsAfterEndTime())
  }

  @Test
  fun `resetUiState clears all fields to default`() {
    val vm = makeVm()

    vm.setTitle("Some Title")
    vm.setDescription("Some Description")
    vm.addParticipant("user1")
    vm.setStartInstant(Instant.parse("2025-03-01T10:00:00Z"))
    vm.setEndInstant(Instant.parse("2025-03-01T11:00:00Z"))
    vm.setRecurrenceMode(RecurrenceStatus.Weekly)

    vm.resetUiState()
    val state = vm.uiState.value

    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
  }

  // ------------------------------------------------------------
  //  Event creation tests
  // ------------------------------------------------------------

  @Test
  fun `addEvent inserts event into repository`() = runTest {
    val vm = makeVm()

    vm.setTitle("Meeting")
    vm.setDescription("Team sync")
    vm.setStartInstant(Instant.now())
    vm.setEndInstant(Instant.now().plus(Duration.ofHours(1)))

    vm.addEvent()
    testDispatcher.scheduler.advanceUntilIdle()

    val events = repository.getAllEvents(selectedOrganizationID)
    assertTrue(events.any { it.title == "Meeting" && it.description == "Team sync" })
  }

  // ------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------

  /** Creates a ViewModel with no authorization (because auth was removed from the app). */
  private fun makeVm(): AddEventViewModel {
    return AddEventViewModel(repository)
  }
}
