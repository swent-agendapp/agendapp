package com.android.sample.ui.calendar

import androidx.compose.ui.graphics.Color
import com.android.sample.model.authorization.AuthorizationService
import com.android.sample.model.calendar.*
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.Role
import com.android.sample.model.organization.SelectedOrganizationRepository
import com.android.sample.ui.calendar.addEvent.AddEventViewModel
import java.time.Duration
import java.time.Instant
import kotlin.test.Ignore
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
 * Unit tests for AddEventViewModel.
 *
 * Uses a test dispatcher to control coroutine execution for deterministic testing.
 */
class AddEventViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var repository: EventRepository

  private val selectedOrganizationID: String = "org123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    repository = EventRepositoryLocal()

    // Set the selected organization in the provider
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial UI state has default values`() {
    val vm = makeAdminVm()
    val state = vm.uiState.value
    assertTrue(state.title.isEmpty())
    assertTrue(state.description.isEmpty())
    assertEquals(0, state.participants.size)
    assertEquals(RecurrenceStatus.OneTime, state.recurrenceMode)
  }

  @Test
  fun `setTitle updates the title in UI state`() {
    val vm = makeAdminVm()
    vm.setTitle("SwEnt Meeting")
    assertEquals("SwEnt Meeting", vm.uiState.value.title)
  }

  @Test
  fun `setColor updates the color in UI state`() {
    val vm = makeAdminVm()
    val newColor = Color(0xFFFF0000)

    vm.setColor(newColor)

    assertEquals(newColor, vm.uiState.value.color)
  }

  @Test
  fun `setDescription updates the description in UI state`() {
    val vm = makeAdminVm()
    vm.setDescription("Standup meeting")
    assertEquals("Standup meeting", vm.uiState.value.description)
  }

  @Test
  fun `setStartInstant updates the start instant in UI state`() {
    val vm = makeAdminVm()
    val newStart = Instant.parse("2025-03-01T10:00:00Z")
    vm.setStartInstant(newStart)
    assertEquals(newStart, vm.uiState.value.startInstant)
  }

  @Test
  fun `setEndInstant updates the end instant in UI state`() {
    val vm = makeAdminVm()
    val newEnd = Instant.parse("2025-03-01T11:00:00Z")
    vm.setEndInstant(newEnd)
    assertEquals(newEnd, vm.uiState.value.endInstant)
  }

  @Test
  fun `addParticipant and removeParticipant modify participants set`() {
    val vm = makeAdminVm()

    vm.addParticipant("user1")
    assertTrue(vm.uiState.value.participants.contains("user1"))

    vm.removeParticipant("user1")
    assertFalse(vm.uiState.value.participants.contains("user1"))
  }

  @Test
  fun `allFieldsValid returns false if title or description is blank`() {
    val vm = makeAdminVm()

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
    val vm = makeAdminVm()

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
    val vm = makeAdminVm()
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

  @Test
  fun `addEvent inserts event into repository`() = runTest {
    val vm = makeAdminVm()
    vm.setTitle("Meeting")
    vm.setDescription("Team sync")
    vm.setStartInstant(Instant.now())
    vm.setEndInstant(Instant.now().plus(Duration.ofHours(1)))

    vm.addEvent()
    testDispatcher.scheduler.advanceUntilIdle() // run pending coroutines

    val events = repository.getAllEvents(orgId = selectedOrganizationID)
    assertTrue(events.any { it.title == "Meeting" && it.description == "Team sync" })
  }

  @Test
  fun `admin can add event`() = runTest {
    val vm = makeAdminVm()

    vm.setTitle("Meeting")
    vm.setDescription("Team sync")
    vm.setStartInstant(Instant.now())
    vm.setEndInstant(Instant.now().plus(Duration.ofHours(1)))

    vm.addEvent()
    testDispatcher.scheduler.advanceUntilIdle()

    val events = repository.getAllEvents(orgId = selectedOrganizationID)
    assertTrue(events.any { it.title == "Meeting" && it.description == "Team sync" })
  }

  /** We have to ignore the test for the M2 as this feature is not fully ready yet */
  @Ignore
  fun `employee cannot add event`() = runTest {
    val vm = makeEmployeeVm()

    vm.setTitle("Meeting")
    vm.setDescription("Team sync")
    vm.setStartInstant(Instant.now())
    vm.setEndInstant(Instant.now().plus(Duration.ofHours(1)))

    vm.addEvent()
    testDispatcher.scheduler.advanceUntilIdle()

    val events = repository.getAllEvents(orgId = selectedOrganizationID)
    assertTrue(events.isEmpty())
  }

  /* Helper functions */
  private fun makeAdminVm(): AddEventViewModel {
    val adminAuthServ =
        AuthorizationService(
            repo =
                object : EmployeeRepository {
                  override suspend fun getEmployees(): List<Employee> = emptyList()

                  override suspend fun newEmployee(employee: Employee) {}

                  override suspend fun deleteEmployee(userId: String) {}

                  override suspend fun getMyRole(): Role? = Role.ADMIN
                })
    return AddEventViewModel(repository, authServ = adminAuthServ)
  }

  private fun makeEmployeeVm(): AddEventViewModel {
    val employeeAuthServ =
        AuthorizationService(
            repo =
                object : EmployeeRepository {
                  override suspend fun getEmployees(): List<Employee> = emptyList()

                  override suspend fun newEmployee(employee: Employee) {}

                  override suspend fun deleteEmployee(userId: String) {}

                  override suspend fun getMyRole(): Role? = Role.EMPLOYEE
                })
    return AddEventViewModel(repository, authServ = employeeAuthServ)
  }
}
