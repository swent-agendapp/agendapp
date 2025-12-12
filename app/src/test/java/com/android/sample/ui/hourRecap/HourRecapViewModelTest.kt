package com.android.sample.ui.hourRecap

import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.data.fake.repositories.RepoMethod
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import java.time.Duration
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*

/** Minimal ViewModel test, without complex fake VM providers. */
@OptIn(ExperimentalCoroutinesApi::class)
class HourRecapViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repo: FakeEventRepository
  private lateinit var repo: SimpleFakeEventRepository
  private lateinit var userRepo: UserRepository
  private lateinit var organizationRepository: OrganizationRepositoryLocal

  private lateinit var user: User

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
    orgA = Organization(id = selectedOrganizationID, name = "Org A")

    // Register all users in the repository
    userRepo.newUser(user)
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

    assertTrue(state.workedHours.isEmpty())
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
    val data = listOf("Alice" to 5.0)

    vm.setTestWorkedHours(data)

    assertEquals(data, vm.uiState.value.workedHours)
  }

  @Test
  fun `calculateWorkedHours loads data successfully`() = runTest {
    val vm = makeVm()
    val participantId = "user1"
    val event =
        Event(
            id = "event1",
            organizationId = orgId,
            title = "Test Event",
            description = "Just a test",
            startDate = Instant.EPOCH.minusSeconds(3600), // passé
            endDate = Instant.EPOCH, // passé
            cloudStorageStatuses = emptySet(),
            personalNotes = null,
            participants = setOf(participantId),
            presence = mapOf(participantId to true),
            version = System.currentTimeMillis(),
            recurrenceStatus = RecurrenceStatus.OneTime,
            location = null)
    repo.add(event)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)

    advanceUntilIdle()

    val expectedDuration = Duration.between(event.startDate, event.endDate).toMinutes() / 60.0
    val expectedWorkedHours = listOf(participantId to expectedDuration)

    assertEquals(expectedWorkedHours, vm.uiState.value.workedHours)
  }

  @Test
  fun `calculateWorkedHours sets error on exception`() = runTest {
    val vm = makeVm()
    repo.failMethods.add(RepoMethod.GET_EVENTS_BETWEEN_DATES)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value.errorMsg?.contains("Failed") == true)
  }
}
