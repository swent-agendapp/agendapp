package com.android.sample.ui.replacement

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.replacement.*
import com.android.sample.ui.replacement.employee.ReplacementEmployeeStep
import com.android.sample.ui.replacement.employee.ReplacementEmployeeViewModel
import com.android.sample.ui.theme.EventPalette
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*

// Assisted by AI
@OptIn(ExperimentalCoroutinesApi::class)
class ReplacementEmployeeViewModelExtraCoverageTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var replacementRepo: FakeReplacementRepository
  private lateinit var eventRepo: FakeEventRepository
  private lateinit var vm: ReplacementEmployeeViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    replacementRepo = FakeReplacementRepository()
    eventRepo = FakeEventRepository()

    vm =
        ReplacementEmployeeViewModel(
            replacementRepository = replacementRepo,
            eventRepository = eventRepo,
            myUserId = "EMP001")
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun backToListResetsStepAndClearsState() = runTest {
    vm.goToSelectEvent()
    vm.setSelectedEvent("E99")
    vm.setStartDate(LocalDate.now())
    vm.setEndDate(LocalDate.now().plusDays(1))

    vm.backToList()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    Assert.assertEquals(ReplacementEmployeeStep.LIST, state.step)
    Assert.assertNull(state.selectedEventId)
    Assert.assertNull(state.startDate)
    Assert.assertNull(state.endDate)
  }

  @Test
  fun confirmSelectedEventAndCreateReplacementDoesNothingIfEventIdIsNull() = runTest {
    vm.confirmSelectedEventAndCreateReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
  }

  @Test
  fun confirmSelectedEventAndCreateReplacementSetsErrorWhenEventNotFound() = runTest {
    vm.setSelectedEvent("NOT_EXIST")

    vm.confirmSelectedEventAndCreateReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
    Assert.assertTrue(vm.uiState.value.errorMessage?.contains("not found") == true)
  }

  @Test
  fun confirmDateRangeAndCreateReplacementsDoesNothingIfDatesAreNull() = runTest {
    vm.confirmDateRangeAndCreateReplacements()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
  }

  @Test
  fun confirmDateRangeAndCreateReplacements_createsReplacementsForEvents() = runTest {
    val e1 = sampleEvent("A1")
    val e2 = sampleEvent("A2")

    eventRepo.add(e1)
    eventRepo.add(e2)

    vm.setStartDate(LocalDate.now())
    vm.setEndDate(LocalDate.now().plusDays(1))

    vm.confirmDateRangeAndCreateReplacements()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertEquals(2, replacementRepo.items.size)
  }

  @Test
  fun confirmDateRangeAndCreateReplacements_createsNoReplacementsWhenNoEventsInRepo() = runTest {
    vm.setStartDate(LocalDate.now())
    vm.setEndDate(LocalDate.now().plusDays(1))

    vm.confirmDateRangeAndCreateReplacements()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
  }

  private class FakeReplacementRepository : ReplacementRepository {
    val items = mutableListOf<Replacement>()

    override suspend fun getAllReplacements() = items

    override suspend fun insertReplacement(item: Replacement) {
      items.add(item)
    }

    override suspend fun updateReplacement(itemId: String, item: Replacement) {}

    override suspend fun deleteReplacement(itemId: String) {}

    override suspend fun getReplacementById(itemId: String) = items.find { it.id == itemId }

    override suspend fun getReplacementsByAbsentUser(userId: String) = emptyList<Replacement>()

    override suspend fun getReplacementsBySubstituteUser(userId: String) =
        items.filter { it.substituteUserId == userId }

    override suspend fun getReplacementsByStatus(status: ReplacementStatus) =
        emptyList<Replacement>()
  }

  private class FakeEventRepository : EventRepository {
    private val list = mutableListOf<Event>()

    fun add(event: Event) = list.add(event)

    override fun getNewUid(): String = "fake-uid"

    override suspend fun getAllEvents() = list

    override suspend fun insertEvent(item: Event) {}

    override suspend fun updateEvent(itemId: String, item: Event) {}

    override suspend fun deleteEvent(itemId: String) {}

    override suspend fun getEventById(itemId: String) = list.find { it.id == itemId }

    override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> {
      return list
    }
  }

  private fun sampleEvent(id: String) =
      Event(
          id = id,
          title = "T$id",
          description = "D$id",
          startDate = Instant.now(),
          endDate = Instant.now().plusSeconds(3600),
          participants = setOf(),
          recurrenceStatus = com.android.sample.model.calendar.RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventPalette.Blue,
          version = 1L,
          locallyStoredBy = listOf("X"),
          cloudStorageStatuses = emptySet(),
          personalNotes = null)
}
