package com.android.sample.ui.replacement

import com.android.sample.model.calendar.Event
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.model.replacement.*
import com.android.sample.ui.calendar.replacementEmployee.*
import com.android.sample.ui.theme.EventPalette
import com.android.sample.utils.FakeEventRepository
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

  private val selectedOrganizationId = "ORG1"

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

    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ----------------------------------------------------------
  // 1. backToList() — test coverage
  // ----------------------------------------------------------
  @Test
  fun backToListResetsStepAndClearsState() = runTest {
    vm.goToCreateOptions()
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

  // ----------------------------------------------------------
  // 2. confirmSelectedEventAndCreateReplacement() — eventId null
  // ----------------------------------------------------------
  @Test
  fun confirmSelectedEventAndCreateReplacementDoesNothingIfEventIdIsNull() = runTest {
    vm.confirmSelectedEventAndCreateReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
  }

  // ----------------------------------------------------------
  // 3. confirmSelectedEventAndCreateReplacement() — event not found
  // ----------------------------------------------------------
  @Test
  fun confirmSelectedEventAndCreateReplacementSetsErrorWhenEventNotFound() = runTest {
    vm.setSelectedEvent("NOT_EXIST")

    vm.confirmSelectedEventAndCreateReplacement()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
    Assert.assertTrue(vm.uiState.value.errorMessage?.contains("not found") == true)
  }

  // ----------------------------------------------------------
  // 4. confirmDateRangeAndCreateReplacements() — start or end null
  // ----------------------------------------------------------
  @Test
  fun confirmDateRangeAndCreateReplacementsDoesNothingIfDatesAreNull() = runTest {
    vm.confirmDateRangeAndCreateReplacements()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
  }

  // ----------------------------------------------------------
  // 5. confirmDateRangeAndCreateReplacements() — happy path
  // ----------------------------------------------------------
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

  // ----------------------------------------------------------
  // 6. findEventsInRange — indirectly covered by calling confirmDateRange
  // ----------------------------------------------------------
  @Test
  fun findEventsInRangeIsExecutedViaConfirmDateRangeAndCreateReplacements() = runTest {
    vm.setStartDate(LocalDate.now())
    vm.setEndDate(LocalDate.now().plusDays(1))

    // eventRepo empty → findEventsInRange returns empty list
    vm.confirmDateRangeAndCreateReplacements()
    testDispatcher.scheduler.advanceUntilIdle()

    Assert.assertTrue(replacementRepo.items.isEmpty())
  }

  // ----------------------------------------------------------
  // Helper classes
  // ----------------------------------------------------------
  private class FakeReplacementRepository : ReplacementRepository {
    val items = mutableListOf<Replacement>()

    override suspend fun getAllReplacements(orgId: String) = items

    override suspend fun insertReplacement(orgId: String, item: Replacement) {
      items.add(item)
    }

    override suspend fun updateReplacement(orgId: String, itemId: String, item: Replacement) {}

    override suspend fun deleteReplacement(orgId: String, itemId: String) {}

    override suspend fun getReplacementById(orgId: String, itemId: String) =
        items.find { it.id == itemId }

    override suspend fun getReplacementsByAbsentUser(orgId: String, userId: String) =
        emptyList<Replacement>()

    override suspend fun getReplacementsBySubstituteUser(orgId: String, userId: String) =
        items.filter { it.substituteUserId == userId }

    override suspend fun getReplacementsByStatus(orgId: String, status: ReplacementStatus) =
        emptyList<Replacement>()
  }

  private fun sampleEvent(id: String) =
      Event(
          id = id,
          organizationId = selectedOrganizationId,
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
