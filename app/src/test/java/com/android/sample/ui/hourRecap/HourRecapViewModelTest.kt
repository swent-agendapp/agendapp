package com.android.sample.ui.hourRecap

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.calendar.Event
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

/** Minimal fake repository implementing only what HourRecapViewModel needs. */
class SimpleFakeEventRepository : EventRepository {
  var result: List<Pair<String, Double>> = emptyList()
  var shouldThrow = false

  override fun getNewUid(): String {
    error("Not needed in test")
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    error("Not needed in test")
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    error("Not needed in test")
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    error("Not needed in test")
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    error("Not needed in test")
  }

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    error("Not needed in test")
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    error("Not needed in test")
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    if (shouldThrow) throw RuntimeException("error!")
    return result
  }
}

/** Minimal ViewModel test, without complex fake VM providers. */
@OptIn(ExperimentalCoroutinesApi::class)
class HourRecapViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
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
    repo = SimpleFakeEventRepository()
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
    repo.result = listOf("Bob" to 10.5)

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)
    advanceUntilIdle()
    assertEquals(repo.result, vm.uiState.value.workedHours)
  }

  @Test
  fun `calculateWorkedHours sets error on exception`() = runTest {
    val vm = makeVm()
    repo.shouldThrow = true

    vm.calculateWorkedHours(start = Instant.EPOCH, end = Instant.EPOCH)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value.errorMsg?.contains("Failed") == true)
  }
}
