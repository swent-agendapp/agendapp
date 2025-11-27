package com.android.sample.ui.calendar

import android.app.Application
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

    repositoryEvent = EventRepositoryLocal()
    repositoryMap = MapRepositoryLocal()

    viewModel = CalendarViewModel(app = app, eventRepository = repositoryEvent, mapRepository = repositoryMap)

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
}
