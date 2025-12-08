package com.android.sample.ui.calendar

import android.app.Application
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.data.local.repositories.EventRepositoryInMemory
import com.android.sample.model.calendar.*
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.MapRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import io.mockk.mockk
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
/**
 * Unit tests for CalendarViewModel.
 *
 * Uses a test dispatcher to control coroutine execution for deterministic testing.
 */
class CalendarViewModelTest {

  // StandardTestDispatcher allows manual control over coroutine execution in tests.
  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repositoryEvent: EventRepository
  private lateinit var repositoryMap: MapRepository
  private lateinit var app: Application
  private lateinit var viewModel: CalendarViewModel

  private lateinit var event1: Event
  private lateinit var event2: Event

  private val orgId: String = "org123"

  @Before
  fun setUp() {
    // Set the main dispatcher to the test dispatcher before each test.
    // This ensures all coroutines launched on Dispatchers.Main use the test dispatcher.
    Dispatchers.setMain(testDispatcher)

    // Set the selected organization for the tests.
    SelectedOrganizationRepository.changeSelectedOrganization(orgId)

    // Mock Application for CalendarViewModel
    app = mockk<Application>(relaxed = true)

    repositoryEvent = EventRepositoryInMemory()
    repositoryMap = MapRepositoryLocal()

    viewModel =
        CalendarViewModel(
            app = app, eventRepository = repositoryEvent, mapRepository = repositoryMap)

    // Create two sample events for testing.
    event1 =
        createEvent(
            organizationId = orgId,
            title = "Meeting",
            description = "Team sync",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            personalNotes = "Bring laptop")[0]

    event2 =
        createEvent(
            organizationId = orgId,
            title = "Conference",
            description = "Tech event",
            startDate = Instant.parse("2025-02-01T09:00:00Z"),
            endDate = Instant.parse("2025-02-03T18:00:00Z"),
        )[0]

    // Insert the sample events into the repository before each test.
    runTest {
      repositoryEvent.insertEvent(orgId = orgId, item = event1)
      repositoryEvent.insertEvent(orgId = orgId, item = event2)
    }
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher to the original Main dispatcher after each test.
    Dispatchers.resetMain()
  }

  @Test
  fun loadAllEventsShouldLoadAllEventsInUiState() = runTest {
    viewModel.loadAllEvents()
    // Advance the dispatcher to execute all pending coroutines.
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.events.size)
    assertTrue(state.events.any { it.title == "Meeting" })
    assertTrue(state.events.any { it.title == "Conference" })
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
  }

  @Test
  fun loadEventsBetweenShouldFilterEventsByDateRange() = runTest {
    val start = Instant.parse("2025-01-01T00:00:00Z")
    val end = Instant.parse("2025-01-31T23:59:59Z")

    viewModel.loadEventsBetween(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.events.size)
    assertEquals("Meeting", state.events.first().title)
    assertNull(state.errorMsg)
    assertFalse(state.isLoading)
  }

  @Test
  fun loadEventsBetweenShouldReturnEmptyWhenNoEventsInRange() = runTest {
    val start = Instant.parse("2024-12-01T00:00:00Z")
    val end = Instant.parse("2024-12-31T23:59:59Z")

    viewModel.loadEventsBetween(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.events.isEmpty())
    assertNull(state.errorMsg)
    assertFalse(state.isLoading)
  }

  @Test
  fun clearErrorMsgShouldResetErrorMsg() = runTest {
    viewModel.loadEventsBetween(
        start = Instant.parse("2025-03-01T00:00:00Z"), end = Instant.parse("2025-02-01T00:00:00Z"))
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun calculateWorkedHoursShouldCalculateCorrectlyForPastEvents() = runTest {
    // Past event (relative to now)
    // We need to ensure the event is considered "past" by the ViewModel.
    // Since ViewModel uses Instant.now(), and we can't easily mock it without dependency injection
    // change,
    // we will create an event very far in the past.
    val pastStart = Instant.parse("2000-01-01T10:00:00Z")
    val pastEnd = Instant.parse("2000-01-01T12:00:00Z") // 2 hours

    val pastEvent =
        createEvent(
            organizationId = orgId,
            title = "Past Meeting",
            startDate = pastStart,
            endDate = pastEnd,
            participants = setOf("user1", "user2"),
            presence = mapOf("user1" to true, "user2" to false))[0]

    repositoryEvent.insertEvent(orgId, pastEvent)

    viewModel.calculateWorkedHours(pastStart.minusSeconds(1), pastEnd.plusSeconds(1))
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.uiState.value.workedHours

    // user1: 2 hours, user2: 0 hours (not present)
    val user1Hours = result.find { it.first == "user1" }?.second ?: 0.0
    val user2Hours = result.find { it.first == "user2" }?.second ?: 0.0

    assertEquals(2.0, user1Hours, 0.01)
    assertEquals(0.0, user2Hours, 0.01)
  }

  @Test
  fun calculateWorkedHoursShouldCalculateCorrectlyForFutureEvents() = runTest {
    // Future event (relative to now)
    val futureStart = Instant.now().plusSeconds(3600) // 1 hour from now
    val futureEnd = futureStart.plusSeconds(7200) // 2 hours duration

    val futureEvent =
        createEvent(
            organizationId = orgId,
            title = "Future Meeting",
            startDate = futureStart,
            endDate = futureEnd,
            participants = setOf("user1", "user2"),
            presence = emptyMap() // Presence shouldn't matter for future events
            )[0]

    repositoryEvent.insertEvent(orgId, futureEvent)

    viewModel.calculateWorkedHours(futureStart.minusSeconds(1), futureEnd.plusSeconds(1))
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.uiState.value.workedHours

    // user1: 2 hours, user2: 2 hours (assumed present)
    val user1Hours = result.find { it.first == "user1" }?.second ?: 0.0
    val user2Hours = result.find { it.first == "user2" }?.second ?: 0.0

    assertEquals(2.0, user1Hours, 0.01)
    assertEquals(2.0, user2Hours, 0.01)
  }

  @Test
  fun calculateWorkedHoursShouldAggregateHoursFromMultipleEvents() = runTest {
    val start = Instant.parse("2000-01-01T00:00:00Z")
    val end = Instant.parse("2000-01-02T00:00:00Z")

    val event1 =
        createEvent(
            organizationId = orgId,
            title = "Event 1",
            startDate = Instant.parse("2000-01-01T10:00:00Z"),
            endDate = Instant.parse("2000-01-01T11:00:00Z"), // 1 hour
            participants = setOf("user1"),
            presence = mapOf("user1" to true))[0]

    val event2 =
        createEvent(
            organizationId = orgId,
            title = "Event 2",
            startDate = Instant.parse("2000-01-01T14:00:00Z"),
            endDate = Instant.parse("2000-01-01T16:00:00Z"), // 2 hours
            participants = setOf("user1"),
            presence = mapOf("user1" to true))[0]

    repositoryEvent.insertEvent(orgId, event1)
    repositoryEvent.insertEvent(orgId, event2)

    viewModel.calculateWorkedHours(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.uiState.value.workedHours

    val user1Hours = result.find { it.first == "user1" }?.second ?: 0.0
    assertEquals(3.0, user1Hours, 0.01)
  }

  @Test
  fun refreshEventsShouldSetRefreshingStateAndLoadNewEvents() = runTest {
    val start = Instant.parse("2025-01-01T00:00:00Z")
    val end = Instant.parse("2025-01-31T23:59:59Z")

    // First, load initial events
    viewModel.loadEventsBetween(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify initial state
    var state = viewModel.uiState.value
    assertEquals(1, state.events.size)

    // Add a new event to repository
    val newEvent =
        createEvent(
            organizationId = orgId,
            title = "New Event",
            startDate = Instant.parse("2025-01-15T10:00:00Z"),
            endDate = Instant.parse("2025-01-15T11:00:00Z"))[0]
    repositoryEvent.insertEvent(orgId, newEvent)

    // Call refresh and complete it
    viewModel.refreshEvents(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    // Check final state
    state = viewModel.uiState.value
    assertFalse(state.isRefreshing)
    assertEquals(2, state.events.size)
    assertTrue(state.events.any { it.title == "New Event" })
    assertNull(state.errorMsg)
  }

  @Test
  fun refreshEventsShouldClearErrorMessage() = runTest {
    val start = Instant.parse("2025-01-01T00:00:00Z")
    val end = Instant.parse("2025-01-31T23:59:59Z")

    // Set an error first
    viewModel.setErrorMsg("Previous error")
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify error is set
    assertNotNull(viewModel.uiState.value.errorMsg)

    // Refresh should clear error on success
    viewModel.refreshEvents(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    // Error should be cleared
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun refreshEventsShouldNotChangeLoadingState() = runTest {
    val start = Instant.parse("2025-01-01T00:00:00Z")
    val end = Instant.parse("2025-01-31T23:59:59Z")

    viewModel.loadEventsBetween(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    // Ensure initial loading is false
    assertFalse(viewModel.uiState.value.isLoading)

    viewModel.refreshEvents(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    // Loading should still be false (only refreshing should be used)
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun refreshEventsShouldFetchEventsForSpecifiedDateRange() = runTest {
    // Add event outside the refresh range
    val outsideEvent =
        createEvent(
            organizationId = orgId,
            title = "Outside Event",
            startDate = Instant.parse("2025-03-01T10:00:00Z"),
            endDate = Instant.parse("2025-03-01T11:00:00Z"))[0]
    repositoryEvent.insertEvent(orgId, outsideEvent)

    val start = Instant.parse("2025-01-01T00:00:00Z")
    val end = Instant.parse("2025-01-31T23:59:59Z")

    viewModel.refreshEvents(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should only contain events within the specified range
    assertEquals(1, state.events.size)
    assertEquals("Meeting", state.events.first().title)
  }
}
