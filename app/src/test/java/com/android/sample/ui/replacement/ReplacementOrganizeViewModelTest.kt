package com.android.sample.ui.replacement

import com.android.sample.model.authentication.User
import com.android.sample.model.authorization.AuthorizationService
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.OrganizationRepository
import com.android.sample.model.organization.OrganizationRepositoryLocal
import com.android.sample.model.organization.Role
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryLocal
import com.android.sample.ui.replacement.organize.ReplacementOrganizeStep
import com.android.sample.ui.replacement.organize.ReplacementOrganizeViewModel
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

// Tests written by AI

@OptIn(ExperimentalCoroutinesApi::class)
class ReplacementOrganizeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var eventRepo: EventRepository
  private lateinit var orgRepo: OrganizationRepository
  private lateinit var replacementRepo: ReplacementRepository
  private lateinit var event1: Event

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    eventRepo = EventRepositoryLocal()
    orgRepo = OrganizationRepositoryLocal()
    replacementRepo = ReplacementRepositoryLocal()

    // Create a sample event for testing.
    event1 =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            personalNotes = "Bring laptop")
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Helper: admin VM */
  private fun makeAdminVm(): ReplacementOrganizeViewModel {
    val adminAuthz =
        AuthorizationService(
            repo =
                object : EmployeeRepository {
                  override suspend fun getEmployees(): List<Employee> = emptyList()

                  override suspend fun newEmployee(employee: Employee) {}

                  override suspend fun deleteEmployee(userId: String) {}

                  override suspend fun getMyRole(): Role? = Role.ADMIN
                })
    return ReplacementOrganizeViewModel(
        eventRepository = eventRepo,
        organizationRepository = orgRepo,
        replacementRepository = replacementRepo,
        authServ = adminAuthz)
  }

  /** Helper: employee VM */
  private fun makeEmployeeVm(): ReplacementOrganizeViewModel {
    val employeeAuthz =
        AuthorizationService(
            repo =
                object : EmployeeRepository {
                  override suspend fun getEmployees(): List<Employee> = emptyList()

                  override suspend fun newEmployee(employee: Employee) {}

                  override suspend fun deleteEmployee(userId: String) {}

                  override suspend fun getMyRole(): Role? = Role.EMPLOYEE
                })
    return ReplacementOrganizeViewModel(
        eventRepository = eventRepo,
        organizationRepository = orgRepo,
        replacementRepository = replacementRepo,
        authServ = employeeAuthz)
  }

  // ----------------------------------------------------------------------
  // UI STATE BASICS
  // ----------------------------------------------------------------------

  @Test
  fun `initial state has default values`() {
    val vm = makeAdminVm()
    val state = vm.uiState.value

    assertTrue(state.memberList.isEmpty())
    assertNull(state.selectedMember)
    assertTrue(state.selectedEvents.isEmpty())
    assertEquals(ReplacementOrganizeStep.SelectSubstitute, state.step)
  }

  @Test
  fun `goToStep updates the current step`() {
    val vm = makeAdminVm()
    vm.goToStep(ReplacementOrganizeStep.SelectEvents)
    assertEquals(ReplacementOrganizeStep.SelectEvents, vm.uiState.value.step)
  }

  @Test
  fun `setMemberSearchQuery updates the query`() {
    val vm = makeAdminVm()
    vm.setMemberSearchQuery("john")
    assertEquals("john", vm.uiState.value.memberSearchQuery)
  }

  @Test
  fun `setSelectedMember updates selected member`() {
    val vm = makeAdminVm()
    val member = User(id = "123", displayName = "Alice", email = "alice@example.com")
    vm.setSelectedMember(member)
    assertEquals(member, vm.uiState.value.selectedMember)
  }

  @Test
  fun `setStartInstant updates start date`() {
    val vm = makeAdminVm()
    val now = Instant.now()
    vm.setStartInstant(now)
    assertEquals(now, vm.uiState.value.startInstant)
  }

  @Test
  fun `setEndInstant updates end date`() {
    val vm = makeAdminVm()
    val now = Instant.now()
    vm.setEndInstant(now)
    assertEquals(now, vm.uiState.value.endInstant)
  }

  // ----------------------------------------------------------------------
  // MEMBER LOADING
  // ----------------------------------------------------------------------

  @Test
  fun `loadOrganizationMembers loads mock members`() = runTest {
    val vm = makeAdminVm()
    vm.loadOrganizationMembers()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.uiState.value.memberList.isNotEmpty())
  }

  // ----------------------------------------------------------------------
  // EVENT SELECTION
  // ----------------------------------------------------------------------

  @Test
  fun `addSelectedEvent adds event only once`() {
    val vm = makeAdminVm()

    val e = event1

    vm.addSelectedEvent(e)
    vm.addSelectedEvent(e)

    val selected = vm.uiState.value.selectedEvents
    assertEquals(1, selected.size)
    assertEquals(e.id, selected.first().id)
  }

  @Test
  fun `removeSelectedEvent removes event`() {
    val vm = makeAdminVm()
    val e = event1

    vm.addSelectedEvent(e)
    vm.removeSelectedEvent(e)

    assertTrue(vm.uiState.value.selectedEvents.isEmpty())
  }

  @Test
  fun `toggleSelectedEvent adds then removes`() {
    val vm = makeAdminVm()
    val e = event1

    vm.toggleSelectedEvent(e)
    assertTrue(vm.uiState.value.selectedEvents.contains(e))

    vm.toggleSelectedEvent(e)
    assertFalse(vm.uiState.value.selectedEvents.contains(e))
  }

  // ----------------------------------------------------------------------
  // DATE RANGE VALIDATION
  // ----------------------------------------------------------------------

  @Test
  fun `dateRangeValid returns true only when end is after start`() {
    val vm = makeAdminVm()
    val now = Instant.now()

    vm.setStartInstant(now)
    vm.setEndInstant(now.plusSeconds(3600))
    assertTrue(vm.dateRangeValid())

    vm.setEndInstant(now.minusSeconds(3600))
    assertFalse(vm.dateRangeValid())
  }

  // ----------------------------------------------------------------------
  // REPLACEMENT CREATION
  // ----------------------------------------------------------------------

  @Test
  fun `addReplacement sets error when no selected member`() = runTest {
    val vm = makeAdminVm()
    vm.addReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("No absent member selected.", vm.uiState.value.errorMsg)
  }

  @Test
  fun `admin can insert replacement for selected events`() = runTest {
    val vm = makeAdminVm()
    val member = User("1", "John Doe", "john.doe@example.com")
    vm.setSelectedMember(member)

    // Add event selection
    val event = event1
    vm.addSelectedEvent(event)

    vm.addReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    val replacements = replacementRepo.getAllReplacements()
    assertEquals(1, replacements.size)
    assertEquals("1", replacements.first().absentUserId)
  }

  @Test
  fun `employee cannot create replacement (authorization fails)`() = runTest {
    val vm = makeEmployeeVm()
    val member = User("1", "John Doe", "john.doe@example.com")
    vm.setSelectedMember(member)
    vm.addSelectedEvent(event1)

    vm.addReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("You are not allowed to organize replacements !", vm.uiState.value.errorMsg)
    assertTrue(replacementRepo.getAllReplacements().isEmpty())
  }

  @Test
  fun `resetUiState resets all values`() {
    val vm = makeAdminVm()

    vm.setMemberSearchQuery("abc")
    vm.setSelectedMember(User("1", "Test", "user@example.com"))
    vm.addSelectedEvent(event1)
    vm.goToStep(ReplacementOrganizeStep.SelectProcessMoment)

    vm.resetUiState()
    val state = vm.uiState.value

    assertTrue(state.memberSearchQuery.isEmpty())
    assertTrue(state.selectedEvents.isEmpty())
    assertNull(state.selectedMember)
    assertEquals(ReplacementOrganizeStep.SelectSubstitute, state.step)
  }
}
