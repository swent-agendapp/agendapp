package com.android.sample.ui.replacement

import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryInMemory
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import com.android.sample.model.replacement.ReplacementRepository
import com.android.sample.model.replacement.ReplacementRepositoryLocal
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReplacementOrganizeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var eventRepo: EventRepository
  private lateinit var orgRepo: OrganizationRepository
  private lateinit var replacementRepo: ReplacementRepository
  private lateinit var event1: Event

  private val selectedOrganizationID = "org123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    eventRepo = EventRepositoryInMemory()
    orgRepo = OrganizationRepositoryLocal()
    replacementRepo = ReplacementRepositoryLocal()

    event1 =
        createEvent(
            organizationId = selectedOrganizationID,
            title = "Meeting",
            description = "Team sync",
            startDate = Instant.parse("2025-01-10T10:00:00Z"),
            endDate = Instant.parse("2025-01-10T11:00:00Z"),
            personalNotes = "Bring laptop")[0]

    SelectedOrganizationVMProvider.viewModel.selectOrganization(selectedOrganizationID)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** Helper VM (authorization removed everywhere) */
  private fun makeVm(): ReplacementOrganizeViewModel {
    return ReplacementOrganizeViewModel(
        eventRepository = eventRepo, replacementRepository = replacementRepo)
  }

  // ----------------------------------------------------------------------
  // UI State Basics
  // ----------------------------------------------------------------------

  @Test
  fun `initial state has default values`() {
    val vm = makeVm()
    val state = vm.uiState.value

    assertTrue(state.memberList.isEmpty())
    assertNull(state.selectedMember)
    assertTrue(state.selectedEvents.isEmpty())
    assertEquals(ReplacementOrganizeStep.SelectSubstitute, state.step)
  }

  @Test
  fun `goToStep updates the current step`() {
    val vm = makeVm()
    vm.goToStep(ReplacementOrganizeStep.SelectEvents)
    assertEquals(ReplacementOrganizeStep.SelectEvents, vm.uiState.value.step)
  }

  @Test
  fun `setMemberSearchQuery updates query`() {
    val vm = makeVm()
    vm.setMemberSearchQuery("john")
    assertEquals("john", vm.uiState.value.memberSearchQuery)
  }

  @Test
  fun `setSelectedMember updates selected member`() {
    val vm = makeVm()
    val user = User("123", "Alice", "alice@example.com")
    vm.setSelectedMember(user)
    assertEquals(user, vm.uiState.value.selectedMember)
  }

  @Test
  fun `setStartInstant updates start date`() {
    val vm = makeVm()
    val now = Instant.now()
    vm.setStartInstant(now)
    assertEquals(now, vm.uiState.value.startInstant)
  }

  @Test
  fun `setEndInstant updates end date`() {
    val vm = makeVm()
    val now = Instant.now()
    vm.setEndInstant(now)
    assertEquals(now, vm.uiState.value.endInstant)
  }

  // ----------------------------------------------------------------------
  // Member Loading
  // ----------------------------------------------------------------------

  @Test
  fun `loadOrganizationMembers loads mock data`() = runTest {
    val vm = makeVm()
    vm.loadOrganizationMembers()
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(vm.uiState.value.memberList.isNotEmpty())
  }

  // ----------------------------------------------------------------------
  // Event Selection
  // ----------------------------------------------------------------------

  @Test
  fun `addSelectedEvent adds event only once`() {
    val vm = makeVm()
    vm.addSelectedEvent(event1)
    vm.addSelectedEvent(event1)

    assertEquals(1, vm.uiState.value.selectedEvents.size)
  }

  @Test
  fun `removeSelectedEvent removes event`() {
    val vm = makeVm()
    vm.addSelectedEvent(event1)
    vm.removeSelectedEvent(event1)
    assertTrue(vm.uiState.value.selectedEvents.isEmpty())
  }

  @Test
  fun `toggleSelectedEvent adds then removes`() {
    val vm = makeVm()
    vm.toggleSelectedEvent(event1)
    assertTrue(vm.uiState.value.selectedEvents.contains(event1))

    vm.toggleSelectedEvent(event1)
    assertFalse(vm.uiState.value.selectedEvents.contains(event1))
  }

  // ----------------------------------------------------------------------
  // Date Range Validation
  // ----------------------------------------------------------------------

  @Test
  fun `dateRangeValid returns true only when end is after start`() {
    val vm = makeVm()
    val now = Instant.now()

    vm.setStartInstant(now)
    vm.setEndInstant(now.plusSeconds(3600))
    assertTrue(vm.dateRangeValid())

    vm.setEndInstant(now.minusSeconds(3600))
    assertFalse(vm.dateRangeValid())
  }

  // ----------------------------------------------------------------------
  // Replacement Creation
  // ----------------------------------------------------------------------

  @Test
  fun `addReplacement sets error when no selected member`() = runTest {
    val vm = makeVm()
    vm.addReplacement()
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals("No absent member selected.", vm.uiState.value.errorMsg)
  }

  @Test
  fun `addReplacement inserts replacements for selected events`() = runTest {
    val vm = makeVm()
    vm.setSelectedMember(User("1", "John Doe", "john@example.com"))
    vm.addSelectedEvent(event1)

    vm.addReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    val replacements = replacementRepo.getAllReplacements(selectedOrganizationID)
    assertEquals(1, replacements.size)
    assertEquals("1", replacements.first().absentUserId)
  }

  @Test
  fun `resetUiState resets everything`() {
    val vm = makeVm()
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
