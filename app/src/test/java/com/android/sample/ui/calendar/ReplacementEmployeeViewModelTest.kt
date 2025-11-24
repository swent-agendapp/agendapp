package com.android.sample.ui.calendar

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.replacement.*
import com.android.sample.ui.replacement.employee.ReplacementEmployeeViewModel
import com.android.sample.ui.theme.EventPalette
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

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    replacementRepo = FakeReplacementRepository()
    eventRepo = FakeEventRepository()
    vm = makeEmployeeVm()
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
        Replacement(
            absentUserId = "A",
            substituteUserId = employeeId,
            event = e1,
            status = ReplacementStatus.ToProcess))

    repos.insertReplacement(
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

    repos.insertReplacement(r)

    vm.acceptRequest(r.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val updated = repos.getReplacementById(r.id)
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

    repos.insertReplacement(r)

    vm.refuseRequest(r.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val updated = repos.getReplacementById(r.id)
    assertEquals(ReplacementStatus.Declined, updated?.status)
  }

  // ---------------------------------------------------------
  // TEST 5 — Creating replacement for a single event
  // ---------------------------------------------------------
  @Test
  fun `createReplacementForEvent inserts replacement into repo`() = runTest {
    val repos = replacementRepo as FakeReplacementRepository
    val event = sampleEvent("EV123")

    eventRepo.insertEvent(event)

    vm.createReplacementForEvent(event)
    testDispatcher.scheduler.advanceUntilIdle()

    val all = repos.getAllReplacements()
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

    eventRepo.insertEvent(e1)
    eventRepo.insertEvent(e2)
    eventRepo.insertEvent(e3)

    val start = Instant.now()
    val end = start.plusSeconds(10_000)

    vm.createReplacementsForDateRange(start, end)
    testDispatcher.scheduler.advanceUntilIdle()

    val list = repos.getAllReplacements()
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
          title = "T$id",
          description = "D$id",
          startDate = Instant.now().plusSeconds(startOffset),
          endDate = Instant.now().plusSeconds(startOffset + 1800),
          participants = emptySet(),
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventPalette.Blue,
          version = 1L,
          locallyStoredBy = emptyList(),
          cloudStorageStatuses = emptySet(),
          personalNotes = null)

  private fun makeEmployeeVm(): ReplacementEmployeeViewModel {

    return ReplacementEmployeeViewModel(replacementRepo, eventRepo)
  }
}

// ========================================================================
// Fake Repositories
// ========================================================================
class FakeReplacementRepository : ReplacementRepository {

  private val storage = mutableListOf<Replacement>()

  override suspend fun getAllReplacements(): List<Replacement> = storage.toList()

  override suspend fun insertReplacement(item: Replacement) {
    storage.add(item)
  }

  override suspend fun updateReplacement(itemId: String, item: Replacement) {
    val idx = storage.indexOfFirst { it.id == itemId }
    if (idx != -1) storage[idx] = item
  }

  override suspend fun deleteReplacement(itemId: String) {
    storage.removeAll { it.id == itemId }
  }

  override suspend fun getReplacementById(itemId: String): Replacement? =
      storage.find { it.id == itemId }

  override suspend fun getReplacementsByAbsentUser(userId: String): List<Replacement> =
      storage.filter { it.absentUserId == userId }

  override suspend fun getReplacementsBySubstituteUser(userId: String): List<Replacement> =
      storage.filter { it.substituteUserId == userId }

  override suspend fun getReplacementsByStatus(status: ReplacementStatus): List<Replacement> =
      storage.filter { it.status == status }
}

class FakeEventRepository : EventRepository {

  private val events = mutableListOf<Event>()

  override fun getNewUid(): String = "fake-uid"

  override suspend fun getAllEvents(): List<Event> = events.toList()

  override suspend fun insertEvent(item: Event) {
    events.add(item)
  }

  override suspend fun updateEvent(itemId: String, item: Event) {}

  override suspend fun deleteEvent(itemId: String) {}

  override suspend fun getEventById(itemId: String): Event? = events.find { it.id == itemId }

  override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> =
      events.filter { e -> e.startDate <= endDate && e.endDate >= startDate }
}
