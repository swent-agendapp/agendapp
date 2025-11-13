package com.android.sample.ui.calendar

import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.*
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
  private lateinit var repository: EventRepository
  private lateinit var viewModel: CalendarViewModel

  private lateinit var event1: Event
  private lateinit var event2: Event

  @Before
  fun setUp() {
    // Set the main dispatcher to the test dispatcher before each test.
    // This ensures all coroutines launched on Dispatchers.Main use the test dispatcher.
    Dispatchers.setMain(testDispatcher)

    repository = EventRepositoryLocal()
    viewModel =
        CalendarViewModel(eventRepository = repository, authRepository = FakeAuthRepository())

    // Create two sample events for testing.
    event1 =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            personalNotes = "Bring laptop")

    event2 =
        createEvent(
            title = "Conference",
            description = "Tech event",
            startDate = Instant.parse("2025-02-01T09:00:00Z"),
            endDate = Instant.parse("2025-02-03T18:00:00Z"),
        )

    // Insert the sample events into the repository before each test.
    runTest {
      repository.insertEvent(event1)
      repository.insertEvent(event2)
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
  fun getEventById_returnsEventFromUiCache_whenAlreadyLoaded() = runTest {
    // loading the events in the UI state
    viewModel.loadAllEvents()
    testDispatcher.scheduler.advanceUntilIdle()

    // Request an existing event by id
    val result = viewModel.getEventById(event1.id)

    // Assert the result event is correct
    assertEquals(event1.id, result.id)
    assertEquals(event1.title, result.title)
  }

  @Test
  fun getEventById_fetchesFromRepository_whenEventNotInScreen() = runTest {
    // The UI state is empty (we do NOT call loadAllEvents()) to simulate that the current week
    // does not contain the event2 (but he's in the repository thanks to the setUp())

    val result = viewModel.getEventById(event2.id)

    // Assert we still find it (even if he was not in the ui state)
    assertEquals(event2.id, result.id)
    assertEquals(event2.title, result.title)
  }

  @Test
  fun getEventById_throws_whenEventDoesNotExist() = runTest {
    val unknownId = "unknown-event-id"

    val ex =
        kotlin.test.assertFailsWith<NoSuchElementException> { viewModel.getEventById(unknownId) }

    // The thrown exception should still mention the unknown id
    assertTrue(ex.message?.contains(unknownId) == true)

    // Now the ViewModel also sets an error message when the event is missing
    val errorMsg = viewModel.uiState.value.errorMsg
    assertNotNull(errorMsg)
    assertTrue(errorMsg!!.contains("Failed to fetch event"))
    assertTrue(errorMsg.contains(unknownId))
  }

  @Test
  fun getParticipantNames_filtersNullBlankAndUnknown() = runTest {
    // Replace the ViewModel with one that uses the provided FakeAuthRepository
    val fakeAuth = FakeAuthRepository(User(id = "u1", displayName = "Alice", email = null))
    viewModel = CalendarViewModel(eventRepository = repository, authRepository = fakeAuth)

    // Insert an event with many participants (only u1 is resolvable by the fake)
    val e =
        createEvent(
            title = "Participants Test",
            description = "",
            startDate = Instant.parse("2025-01-15T12:00:00Z"),
            endDate = Instant.parse("2025-01-15T13:00:00Z"),
            participants = setOf("u1", "u2", "u3", "u4"))
    repository.insertEvent(e)

    // Getting all the participants names
    val names = viewModel.getParticipantNames(e.id)

    // Assert that only the non-blank name is returned and that there is no error message
    assertEquals(listOf("Alice"), names)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun getParticipantNames_returnsEmpty_whenResolvableUserHasNullName() = runTest {
    // Replace the ViewModel with one that uses the provided FakeAuthRepository
    val fakeAuth = FakeAuthRepository(User(id = "u1", displayName = null, email = null))
    viewModel = CalendarViewModel(eventRepository = repository, authRepository = fakeAuth)

    // Create an event with a participant having a NULL displayName
    val e =
        createEvent(
            title = "Null Name",
            description = "",
            startDate = Instant.parse("2025-01-21T09:00:00Z"),
            endDate = Instant.parse("2025-01-21T10:00:00Z"),
            participants = setOf("u1"))
    repository.insertEvent(e)

    // Assert that we get an empty list and no error
    val names = viewModel.getParticipantNames(e.id)
    assertTrue(names.isEmpty())
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun getParticipantNames_returnsEmpty_whenResolvableUserHasBlankName() = runTest {
    // Replace the ViewModel with one that uses the provided FakeAuthRepository
    val fakeAuth = FakeAuthRepository(User(id = "u1", displayName = "   ", email = null))
    viewModel = CalendarViewModel(eventRepository = repository, authRepository = fakeAuth)

    // Create an event with a participant having a BLANK displayName
    val e =
        createEvent(
            title = "Blank Name",
            description = "",
            startDate = Instant.parse("2025-01-22T09:00:00Z"),
            endDate = Instant.parse("2025-01-22T10:00:00Z"),
            participants = setOf("u1"))
    repository.insertEvent(e)

    // Assert that we get an empty list and no error
    val names = viewModel.getParticipantNames(e.id)
    assertTrue(names.isEmpty())
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun resolveDisplayNameForUser_returnsDisplayName_whenExists() = runTest {
    // Replace the ViewModel with one that uses the provided FakeAuthRepository
    val fakeAuth = FakeAuthRepository(User(id = "uA", displayName = "Ana", email = null))
    viewModel = CalendarViewModel(eventRepository = repository, authRepository = fakeAuth)

    // Assert that we get a correct name and no error
    val result = viewModel.resolveDisplayNameForUser("uA")
    assertEquals("Ana", result)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun resolveDisplayNameForUser_returnsNull_whenDisplayNameIsNull() = runTest {
    // With only one user that has a NULL display name
    val fakeAuth = FakeAuthRepository(User(id = "uB", displayName = null, email = null))
    // Replace the ViewModel with one that uses the provided FakeAuthRepository
    viewModel = CalendarViewModel(eventRepository = repository, authRepository = fakeAuth)

    // Assert that the name get is null too and that there is no error
    val result = viewModel.resolveDisplayNameForUser("uB")
    assertNull(result)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun resolveDisplayNameForUser_returnsNull_whenDisplayNameIsBlank() = runTest {
    // With only one user that has a BLANK display name
    val fakeAuth = FakeAuthRepository(User(id = "uC", displayName = "   ", email = null))
    // Replace the ViewModel with one that uses the provided FakeAuthRepository
    viewModel = CalendarViewModel(eventRepository = repository, authRepository = fakeAuth)

    // Assert that the name get is null too and that there is no error
    val result = viewModel.resolveDisplayNameForUser("uC")
    assertNull(result)
    assertNull(viewModel.uiState.value.errorMsg)
  }
}
