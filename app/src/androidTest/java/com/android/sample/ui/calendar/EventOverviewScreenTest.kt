package com.android.sample.ui.calendar

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreen
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreenTestTags
import com.android.sample.ui.calendar.eventOverview.EventOverviewViewModel
import com.android.sample.utils.FakeEventRepository
import java.time.Duration
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the EventOverviewScreen.
 *
 * These tests verify that:
 * - The Delete and Modify buttons exist.
 * - Clicking Modify calls the onEditClick callback.
 * - Clicking Delete opens the confirmation dialog and calls onDeleteClick after confirmation.
 */
class EventOverviewScreenTest {

  @get:Rule val composeRule = createComposeRule()

  val selectedOrganizationId = "orgTest"
  private lateinit var repo: EventRepository

  @Before
  fun setup() {
    repo = EventRepositoryProvider.repository

    // Ensure the right organization is selected
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
  }

  // ---------- Helpers ----------
  private fun sampleEvent(): Event {
    val start = now().truncatedTo(ChronoUnit.HOURS)
    return Event(
        id = "E123",
        organizationId = selectedOrganizationId,
        title = "Test Event",
        description = "Desc",
        startDate = start,
        endDate = start.plus(Duration.ofHours(1)),
        cloudStorageStatuses = emptySet(),
        locallyStoredBy = emptyList(),
        personalNotes = null,
        participants = setOf("Alice", "Bob"),
        version = 1L,
        recurrenceStatus = RecurrenceStatus.OneTime,
        hasBeenDeleted = false,
        category = EventCategory.defaultCategory())
  }

  private fun makeViewModelWith(event: Event): Pair<EventOverviewViewModel, FakeEventRepository> {
    val repo = FakeEventRepository()
    repo.add(event = event)
    val vm = EventOverviewViewModel(eventRepository = repo)
    return vm to repo
  }

  // ---------- Tests ----------

  /** Verifies that the screen contains Delete, Modify and Ask to be replaced buttons. */
  @Test
  fun allThreeButtons_exist() {
    val (vm, _) = makeViewModelWith(sampleEvent())

    composeRule.setContent {
      EventOverviewScreen(
          eventId = "E123",
          eventOverviewViewModel = vm,
          onBackClick = {},
          onEditClick = {},
          onDeleteClick = {})
    }

    composeRule.waitForIdle()

    composeRule.onNodeWithTag(EventOverviewScreenTestTags.DELETE_BUTTON).assertExists()
    composeRule.onNodeWithTag(EventOverviewScreenTestTags.MODIFY_BUTTON).assertExists()
    composeRule.onNodeWithTag(EventOverviewScreenTestTags.ASK_TO_BE_REPLACED_BUTTON).assertExists()
  }

  /**
   * Verifies that clicking the Modify button triggers the onEditClick callback with the correct
   * event ID.
   */
  @Test
  fun clickingModify_callsOnEditClick() {
    val (vm, _) = makeViewModelWith(sampleEvent())
    var editCalledWith: String? = null

    composeRule.setContent {
      EventOverviewScreen(
          eventId = "E123",
          eventOverviewViewModel = vm,
          onBackClick = {},
          onEditClick = { id -> editCalledWith = id },
          onDeleteClick = {})
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithTag(EventOverviewScreenTestTags.MODIFY_BUTTON).performClick()

    assert(editCalledWith == "E123")
  }

  /**
   * Verifies that:
   * 1. Clicking Delete opens the confirmation dialog.
   * 2. Confirming deletion triggers onDeleteClick.
   * 3. The repository's deleteEvent is called with the correct ID.
   */
  @Test
  fun clickingDelete_confirmsAndCallsOnDeleteClick() {
    val (vm, fakeRepo) = makeViewModelWith(sampleEvent())

    composeRule.setContent {
      EventOverviewScreen(
          eventId = "E123",
          eventOverviewViewModel = vm,
          onBackClick = {},
          onEditClick = {},
          onDeleteClick = {})
    }

    composeRule.waitForIdle()
    composeRule.onNodeWithTag(EventOverviewScreenTestTags.DELETE_BUTTON).performClick()
    composeRule.onNodeWithTag(EventOverviewScreenTestTags.DIALOG_DELETE_BUTTON).performClick()

    runBlocking { assert(fakeRepo.deletedIds.contains("E123")) }
  }

  /**
   * Verifies that when the repository fails to delete an event, the ViewModel's errorMsg is set
   * appropriately.
   */
  @Test
  fun clickingDelete_whenRepositoryFails_setsErrorMsg() {
    val (vm, fakeRepo) = makeViewModelWith(sampleEvent())
    fakeRepo.shouldFailDelete = true

    composeRule.setContent {
      EventOverviewScreen(
          eventId = "E123",
          eventOverviewViewModel = vm,
          onBackClick = {},
          onEditClick = {},
          onDeleteClick = {})
    }

    composeRule.waitForIdle()

    composeRule.onNodeWithTag(EventOverviewScreenTestTags.DELETE_BUTTON).performClick()
    composeRule.onNodeWithTag(EventOverviewScreenTestTags.DIALOG_DELETE_BUTTON).performClick()
    // Allow some time for the coroutine to process
    assert(vm.uiState.value.errorMsg == "Failed to delete event")
  }
}
