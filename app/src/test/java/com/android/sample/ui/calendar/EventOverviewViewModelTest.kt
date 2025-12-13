package com.android.sample.ui.calendar

import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryInMemory
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.eventOverview.EventOverviewViewModel
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Unit tests for EventOverviewViewModel.
 *
 * Uses a test dispatcher to control coroutine execution for deterministic testing.
 */
class EventOverviewViewModelTest {

  // StandardTestDispatcher allows manual control over coroutine execution in tests.
  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: EventRepository
  private lateinit var userRepository: UserRepository

  private lateinit var organizationRepository: OrganizationRepository

  private lateinit var user1: User
  private lateinit var user2: User
  private lateinit var organization: Organization

  private lateinit var viewModel: EventOverviewViewModel

  private lateinit var eventWithoutParticipants: Event
  private lateinit var eventWithParticipants: Event

  private val selectedOrganizationID: String = "org123"

  @Before
  fun setUp() = runBlocking {
    // Set the main dispatcher to the test dispatcher before each test.
    Dispatchers.setMain(testDispatcher)

    // Set the selected organization for the tests.
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)

    repository = EventRepositoryInMemory()
    userRepository = UsersRepositoryLocal()

    organizationRepository = OrganizationRepositoryLocal(userRepository)

    user1 =
        User(
            id = "Alice",
            displayName = "Alice",
            email = "alice@example.com",
            organizations = listOf(selectedOrganizationID))
    user2 =
        User(
            id = "Bob",
            displayName = "Bob",
            email = "bob@example.com",
            organizations = listOf(selectedOrganizationID))

    // Register all users in the repository
    userRepository.newUser(user1)
    userRepository.newUser(user2)

    // --- Create organization ---
    organization = Organization(id = selectedOrganizationID, name = "Org A")

    organizationRepository.insertOrganization(organization)

    // Set up user-organization relationships
    userRepository.addAdminToOrganization(user1.id, organization.id)
    userRepository.addAdminToOrganization(user2.id, organization.id)

    // Set the selected organization in the provider (if needed by the app)
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)

    viewModel =
        EventOverviewViewModel(
            eventRepository = repository,
            authRepository = FakeAuthRepository(),
            userRepository = userRepository)

    // Create sample events for testing.
    eventWithoutParticipants =
        createEvent(
            organizationId = selectedOrganizationID,
            title = "Event without participants",
            description = "No participants",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            participants = emptySet())[0]

    eventWithParticipants =
        createEvent(
            organizationId = selectedOrganizationID,
            title = "Event with participants",
            description = "Some participants",
            startDate = Instant.parse("2025-02-01T09:00:00Z"),
            endDate = Instant.parse("2025-02-01T10:00:00Z"),
            participants = setOf("Alice", "Bob"),
            presence = mapOf("Alice" to true, "Bob" to false))[0]

    // Insert the sample events into the repository before each test.
    runTest {
      repository.insertEvent(orgId = selectedOrganizationID, item = eventWithoutParticipants)
      repository.insertEvent(orgId = selectedOrganizationID, item = eventWithParticipants)
    }
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher to the original Main dispatcher after each test.
    Dispatchers.resetMain()
  }

  @Test
  fun loadEvent_WithValidId_ShouldUpdateUiStateWithEvent() = runTest {
    // When we load a known event id, the state should contain this event and no error.
    viewModel.loadEvent(eventWithParticipants.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value

    // Override fields “locallyStoredBy“ and “version“, which are automatically filled by the
    // repository.
    val expectedEvent =
        eventWithParticipants.copy(
            locallyStoredBy = state.event?.locallyStoredBy ?: emptyList(),
            version = state.event?.version ?: 1L)

    assertEquals(expectedEvent, state.event)
    assertEquals(state.participantsNames.size, 2)
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
  }

  @Test(expected = NoSuchElementException::class)
  fun loadEvent_WithUnknownId_ShouldThrowException() = runTest {
    // When the event does not exist, the ViewModel rethrows a NoSuchElementException.
    // Because the exception is thrown from a coroutine managed by runTest,
    // it will escape this test method and satisfy the expected exception.
    viewModel.loadEvent("unknown-id")
    testDispatcher.scheduler.advanceUntilIdle()
  }

  @Test
  fun loadEvent_WhenEventChangesInRepository_ShouldLoadUpdatedEvent() = runTest {
    // First load the event to populate the UI state with the initial version.
    viewModel.loadEvent(eventWithParticipants.id)
    testDispatcher.scheduler.advanceUntilIdle()
    val initialState = viewModel.uiState.value

    // Check that we loaded the expected event the first time (by id and title).
    assertEquals(eventWithParticipants.id, initialState.event?.id)
    assertEquals("Event with participants", initialState.event?.title)

    // Update the event in the repository (same id, different title).
    val updatedEvent = eventWithParticipants.copy(title = "Updated title")
    repository.updateEvent(
        orgId = selectedOrganizationID, itemId = eventWithParticipants.id, item = updatedEvent)

    // Load the same event again and ensure we get the new version from the repository.
    viewModel.loadEvent(eventWithParticipants.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val updatedState = viewModel.uiState.value

    // Same id -> we are still looking at the same logical event.
    assertEquals(eventWithParticipants.id, updatedState.event?.id)
    // Different title -> the ViewModel did not use an old cached version.
    assertEquals("Updated title", updatedState.event?.title)

    assertEquals(updatedState.participantsNames.size, 2)
    assertFalse(updatedState.isLoading)
    assertNull(updatedState.errorMsg)
  }
}
