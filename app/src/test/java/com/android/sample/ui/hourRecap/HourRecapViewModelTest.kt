package com.android.sample.ui.hourRecap

import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.data.fake.repositories.RepoMethod
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*

/** Minimal ViewModel test, without complex fake VM providers. */
@OptIn(ExperimentalCoroutinesApi::class)
class HourRecapViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repo: FakeEventRepository
  private lateinit var userRepo: UserRepository
  private lateinit var organizationRepository: OrganizationRepositoryLocal

  private lateinit var user: User
  private lateinit var otherUser: User

  private lateinit var orgA: Organization
  private val selectedOrganizationID: String = "org123"
  private val selectedOrgFlow = MutableStateFlow(selectedOrganizationID)

  @Before
  fun setup() = runBlocking {
    Dispatchers.setMain(testDispatcher)
    repo = FakeEventRepository()
    userRepo = UsersRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal(userRepository = userRepo)

    organizationRepository = OrganizationRepositoryLocal(userRepository = userRepo)

    // --- Create users ---
    user = User(id = "Bob", displayName = "Bob", email = "adminA@example.com")
    otherUser = User(id = "Alice", displayName = "Alice", email = "alice@example.com")
    orgA = Organization(id = selectedOrganizationID, name = "Org A")

    // Register all users in the repository
    userRepo.newUser(user)
    userRepo.newUser(otherUser)
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)
    organizationRepository.insertOrganization(orgA)
    userRepo.addAdminToOrganization(user.id, selectedOrganizationID)
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
  }

  private fun makeVm(): HourRecapViewModel {
    return HourRecapViewModel(
        eventRepository = repo,
        selectedOrganizationFlow = selectedOrgFlow,
        userRepository = userRepo)
  }

  @Test
  fun `initial state is correct`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertTrue(state.userRecaps.isEmpty())
    assertNull(state.errorMsg)
    assertTrue(!state.isLoading)
  }

  @Test
  fun `setErrorMsg updates error`() {
    val vm = makeVm()

    vm.setErrorMsg("Oops")

    assertEquals("Oops", vm.uiState.value.errorMsg)
  }

  @Test
  fun `clearErrorMsg resets error`() {
    val vm = makeVm()

    vm.setErrorMsg("Err")
    vm.clearErrorMsg()

    assertNull(vm.uiState.value.errorMsg)
  }

  @Test
  fun `setTestWorkedHours updates worked hours`() {
    val vm = makeVm()
    val data =
        listOf(
            HourRecapUserRecap(
                userId = "Alice",
                displayName = "Alice",
                completedHours = 3.0,
                plannedHours = 2.0,
                events = emptyList(),
                extraEventsCount = 0))

    vm.setTestWorkedHours(data)

    assertEquals(data, vm.uiState.value.userRecaps)
  }

  @Test
  fun `calculateWorkedHours loads data successfully`() = runTest {
    val vm = makeVm()
    repo.workedHoursResult = listOf("Bob" to 10.5)
    val start = Instant.now().minus(3, ChronoUnit.DAYS)
    val end = Instant.now().plus(3, ChronoUnit.DAYS)
    val pastEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = Instant.now().minus(1, ChronoUnit.DAYS),
            endDate = Instant.now().minus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id),
            presence = mapOf(user.id to true))
    val futureEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = Instant.now().plus(1, ChronoUnit.DAYS),
            endDate = Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
            participants = setOf(otherUser.id),
            assignedUsers = setOf(user.id, otherUser.id),
            recurrence = RecurrenceStatus.OneTime)
    (pastEvent + futureEvent).forEach { repo.add(it) }

    vm.calculateWorkedHours(start = start, end = end)
    advanceUntilIdle()

    val recaps = vm.uiState.value.userRecaps
    // Both Bob and Alice should appear: Bob has past event + is assigned to future event,
    // Alice participates in and is assigned to future event
    assertEquals(2, recaps.size)
    val bobRecap = recaps.find { it.userId == user.id }
    assertEquals("Bob", bobRecap?.displayName)
    assertEquals(2, bobRecap?.events?.size) // Both past and future events
  }

  @Test
  fun `calculateWorkedHours sets error on exception`() = runTest {
    val vm = makeVm()
    repo.failMethods.add(RepoMethod.CALCULATE_WORKED_HOURS_PAST)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value.errorMsg?.contains("Failed") == true)

    // Clear the failure for other tests
    repo.failMethods.remove(element = RepoMethod.CALCULATE_WORKED_HOURS)
  }

  @Test
  fun `event entry detects past events correctly`() = runTest {
    val vm = makeVm()
    val pastTime = Instant.now().minus(2, ChronoUnit.DAYS)
    val pastEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = pastTime,
            endDate = pastTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))

    repo.workedHoursResult = listOf(user.id to 2.0)
    repo.add(
        createEvent(
                organizationId = selectedOrganizationID,
                startDate = pastTime,
                endDate = pastTime.plus(2, ChronoUnit.HOURS),
                participants = setOf(user.id))
            .first())

    vm.calculateWorkedHours(Instant.now().minus(3, ChronoUnit.DAYS), Instant.now())
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertTrue(events.first().isPast)
  }

  @Test
  fun `event entry detects future events correctly`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(2, ChronoUnit.DAYS)
    val futureEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))

    repo.workedHoursResult = listOf(user.id to 2.0)
    futureEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(3, ChronoUnit.DAYS))
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertTrue(!events.first().isPast)
  }

  @Test
  fun `event entry detects presence for past events`() = runTest {
    val vm = makeVm()
    val pastTime = Instant.now().minus(1, ChronoUnit.DAYS)
    val presentEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = pastTime,
            endDate = pastTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id),
            presence = mapOf(user.id to true))

    repo.workedHoursResult = listOf(user.id to 2.0)
    presentEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now())
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertEquals(true, events.first().wasPresent)
  }

  @Test
  fun `event entry detects absence for past events`() = runTest {
    val vm = makeVm()
    val pastTime = Instant.now().minus(1, ChronoUnit.DAYS)
    val absentEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = pastTime,
            endDate = pastTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id),
            presence = mapOf(user.id to false))

    repo.workedHoursResult = listOf(user.id to 2.0)
    absentEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now())
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertEquals(false, events.first().wasPresent)
  }

  @Test
  fun `calculateWorkedHours includes users with past shifts but no presence`() = runTest {
    val vm = makeVm()
    val pastTime = Instant.now().minus(2, ChronoUnit.DAYS)
    val absentPastEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = pastTime,
            endDate = pastTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id),
            presence = mapOf(user.id to false))

    repo.workedHoursResult = emptyList()
    absentPastEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(
        Instant.now().minus(3, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS))
    advanceUntilIdle()

    val recaps = vm.uiState.value.userRecaps
    assertEquals(1, recaps.size)
    assertEquals(user.id, recaps.first().userId)
    assertEquals(1, recaps.first().events.size)
  }

  @Test
  fun `calculateWorkedHours includes events tracked only in presence map`() = runTest {
    val vm = makeVm()
    val pastTime = Instant.now().minus(2, ChronoUnit.DAYS)
    val presenceOnlyEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = pastTime,
            endDate = pastTime.plus(2, ChronoUnit.HOURS),
            participants = emptySet(),
            assignedUsers = emptySet(),
            presence = mapOf(user.id to false))

    repo.workedHoursResult = emptyList()
    presenceOnlyEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(
        Instant.now().minus(3, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS))
    advanceUntilIdle()

    val recaps = vm.uiState.value.userRecaps
    assertEquals(1, recaps.size)
    assertEquals(user.id, recaps.first().userId)
    assertEquals(1, recaps.first().events.size)
  }

  @Test
  fun `event entry has null presence for future events`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(1, ChronoUnit.DAYS)
    val futureEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))

    repo.workedHoursResult = listOf(user.id to 2.0)
    futureEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS))
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertNull(events.first().wasPresent)
  }

  @Test
  fun `event entry detects wasReplaced correctly`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(1, ChronoUnit.DAYS)
    // User was assigned but not participating (was replaced)
    val replacedEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            assignedUsers = setOf(user.id, otherUser.id),
            participants = setOf(otherUser.id)) // User is assigned but not participating

    repo.workedHoursResult = listOf(user.id to 0.0)
    replacedEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS))
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertTrue(events.first().wasReplaced)
  }

  @Test
  fun `event entry detects tookReplacement correctly`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(1, ChronoUnit.DAYS)
    // OtherUser was assigned but user took their place
    val replacementEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            assignedUsers = setOf(otherUser.id),
            participants = setOf(user.id)) // User is participating but wasn't assigned

    repo.workedHoursResult = listOf(user.id to 2.0)
    replacementEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS))
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertTrue(events.first().tookReplacement)
  }

  @Test
  fun `event entry with no replacement has correct flags`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(1, ChronoUnit.DAYS)
    // Normal event - user is both assigned and participating
    val normalEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            assignedUsers = setOf(user.id),
            participants = setOf(user.id))

    repo.workedHoursResult = listOf(user.id to 2.0)
    normalEvent.forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS))
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertTrue(!events.first().wasReplaced)
    assertTrue(!events.first().tookReplacement)
  }

  @Test
  fun `buildUserRecaps filters events correctly for user`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(1, ChronoUnit.DAYS)
    // Event for user
    val userEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))
    // Event for other user only
    val otherEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime.plus(1, ChronoUnit.HOURS),
            endDate = futureTime.plus(3, ChronoUnit.HOURS),
            participants = setOf(otherUser.id))

    repo.workedHoursResult = listOf(user.id to 2.0, otherUser.id to 2.0)
    (userEvent + otherEvent).forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS))
    advanceUntilIdle()

    val recaps = vm.uiState.value.userRecaps
    // Both users should appear, each with their own event
    assertEquals(2, recaps.size)
    val bobRecap = recaps.find { it.userId == user.id }
    assertEquals(1, bobRecap?.events?.size)
    assertEquals(userEvent.first().id, bobRecap?.events?.first()?.id)
  }

  @Test
  fun `buildUserRecaps ignores deleted events`() = runTest {
    val vm = makeVm()
    val futureTime = Instant.now().plus(1, ChronoUnit.DAYS)

    val activeEvent =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = futureTime,
            endDate = futureTime.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))
    val deletedEvent =
        createEvent(
                organizationId = selectedOrganizationID,
                startDate = futureTime.plus(3, ChronoUnit.HOURS),
                endDate = futureTime.plus(5, ChronoUnit.HOURS),
                participants = setOf(user.id))
            .first()
            .copy(hasBeenDeleted = true)

    repo.workedHoursResult = listOf(user.id to 2.0)
    (activeEvent + listOf(deletedEvent)).forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS))
    advanceUntilIdle()

    val recaps = vm.uiState.value.userRecaps
    assertEquals(1, recaps.size)
    assertEquals(1, recaps.first().events.size)
    assertEquals(activeEvent.first().id, recaps.first().events.first().id)
  }

  @Test
  fun `buildUserRecaps sorts events by startDate`() = runTest {
    val vm = makeVm()
    val time1 = Instant.now().plus(1, ChronoUnit.DAYS)
    val time2 = Instant.now().plus(2, ChronoUnit.DAYS)
    val time3 = Instant.now().plus(3, ChronoUnit.DAYS)

    val event3 =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = time3,
            endDate = time3.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))
    val event1 =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = time1,
            endDate = time1.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))
    val event2 =
        createEvent(
            organizationId = selectedOrganizationID,
            startDate = time2,
            endDate = time2.plus(2, ChronoUnit.HOURS),
            participants = setOf(user.id))

    // Add events in random order
    repo.workedHoursResult = listOf(user.id to 6.0)
    (event3 + event1 + event2).forEach { repo.add(it) }

    vm.calculateWorkedHours(Instant.now(), Instant.now().plus(4, ChronoUnit.DAYS))
    advanceUntilIdle()

    val events = vm.uiState.value.userRecaps.first().events
    assertEquals(3, events.size)
    assertEquals(event1.first().id, events[0].id)
    assertEquals(event2.first().id, events[1].id)
    assertEquals(event3.first().id, events[2].id)
  }
}
