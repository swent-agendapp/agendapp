package com.android.sample.ui.calendar

import com.android.sample.data.fake.repositories.FakeEventRepository
import com.android.sample.data.fake.repositories.FakeReplacementRepository
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.model.replacement.*
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeViewModel
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

// Assisted by AI

/** Unit tests for [ReplacementEmployeeViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReplacementEmployeeViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var replacementRepo: ReplacementRepository
  private lateinit var eventRepo: EventRepository
  private lateinit var vm: ReplacementEmployeeViewModel

  private val employeeId = "EMP001"

  private val selectedOrganizationID = "ORG001"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    replacementRepo = FakeReplacementRepository()
    eventRepo = FakeEventRepository()
    vm = makeEmployeeVm()

    // Set selected organization in the VM provider
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ---------------------------------------------------------
  // TEST 1 — Initial state
  // ---------------------------------------------------------
  @Test
  fun `initial incoming requests list is empty`() {
    assertTrue(vm.uiState.value.incomingRequests.isEmpty())
  }

  // ---------------------------------------------------------
  // TEST 2 — Loading incoming requests for employee
  // ---------------------------------------------------------
  @Test
  fun `refreshIncomingRequests loads only requests for current employee`() = runTest {
    val repos = replacementRepo as FakeReplacementRepository

    val e1 = sampleEvent("E100")
    val e2 = sampleEvent("E200")

    repos.insertReplacement(
        selectedOrganizationID,
        Replacement(
            absentUserId = "A",
            substituteUserId = employeeId,
            event = e1,
            status = ReplacementStatus.ToProcess))

    repos.insertReplacement(
        selectedOrganizationID,
        Replacement(
            absentUserId = "A",
            substituteUserId = "SOMEONE_ELSE",
            event = e2,
            status = ReplacementStatus.ToProcess))

    vm.refreshIncomingRequests()
    testDispatcher.scheduler.advanceUntilIdle()

    val list = vm.uiState.value.incomingRequests
    assertEquals(1, list.size)
    assertEquals("E100", list[0].event.id)
  }

  // ---------------------------------------------------------
  // TEST 3 — Accepting a replacement request
  // ---------------------------------------------------------
  @Test
  fun `acceptRequest updates status to Accepted`() = runTest {
    val repos = replacementRepo as FakeReplacementRepository
    val event = sampleEvent("E999")

    val r =
        Replacement(
            absentUserId = "A",
            substituteUserId = employeeId,
            event = event,
            status = ReplacementStatus.ToProcess)

    repos.insertReplacement(selectedOrganizationID, r)

    vm.acceptRequest(r.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val updated = repos.getReplacementById(selectedOrganizationID, r.id)
    assertEquals(ReplacementStatus.Accepted, updated?.status)
  }

  // ---------------------------------------------------------
  // TEST 4 — Refusing a replacement request
  // ---------------------------------------------------------
  @Test
  fun `refuseRequest updates status to Declined`() = runTest {
    val repos = replacementRepo as FakeReplacementRepository
    val event = sampleEvent("E101")

    val r =
        Replacement(
            absentUserId = "A",
            substituteUserId = employeeId,
            event = event,
            status = ReplacementStatus.ToProcess)

    repos.insertReplacement(selectedOrganizationID, r)

    vm.refuseRequest(r.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val updated = repos.getReplacementById(selectedOrganizationID, r.id)
    assertEquals(ReplacementStatus.Declined, updated?.status)
  }

  // ---------------------------------------------------------
  // TEST 5 — Creating replacement for a single event
  // ---------------------------------------------------------
  @Test
  fun `createReplacementForEvent inserts replacement into repo`() = runTest {
    val repos = replacementRepo as FakeReplacementRepository
    val event = sampleEvent("EV123")

    eventRepo.insertEvent(orgId = selectedOrganizationID, item = event)

    vm.createReplacementForEvent(event)
    testDispatcher.scheduler.advanceUntilIdle()

    val all = repos.getAllReplacements(selectedOrganizationID)
    assertEquals(1, all.size)
    assertEquals("EV123", all[0].event.id)
    assertEquals(employeeId, all[0].absentUserId)
  }

  // ---------------------------------------------------------
  // TEST 6 — Creating replacements for date range
  // ---------------------------------------------------------
  @Test
  fun `createReplacementsForDateRange creates one replacement per matched event`() = runTest {
    val repos = replacementRepo as FakeReplacementRepository

    val e1 = sampleEvent("D1", startOffset = 0)
    val e2 = sampleEvent("D2", startOffset = 3_600)
    val e3 = sampleEvent("D3", startOffset = 50_000) // out of range

    eventRepo.insertEvent(orgId = selectedOrganizationID, item = e1)
    eventRepo.insertEvent(orgId = selectedOrganizationID, item = e2)
    eventRepo.insertEvent(orgId = selectedOrganizationID, item = e3)

    val start = Instant.now()
    val end = start.plusSeconds(10_000)

    vm.createReplacementsForDateRange(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val list = repos.getAllReplacements(selectedOrganizationID)
    assertEquals(2, list.size)
    assertTrue(list.any { it.event.id == "D1" })
    assertTrue(list.any { it.event.id == "D2" })
  }

  // -------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------
  private fun sampleEvent(id: String, startOffset: Long = 0): Event =
      Event(
          id = id,
          organizationId = selectedOrganizationID,
          title = "T$id",
          description = "D$id",
          startDate = Instant.now().plusSeconds(startOffset),
          endDate = Instant.now().plusSeconds(startOffset + 1800),
          participants = emptySet(),
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          category = EventCategory.defaultCategory(),
          version = 1L,
          locallyStoredBy = emptyList(),
          cloudStorageStatuses = emptySet(),
          personalNotes = null,
          location = null)

  private fun makeEmployeeVm(): ReplacementEmployeeViewModel {

    return ReplacementEmployeeViewModel(replacementRepo, eventRepo)
  }
}
