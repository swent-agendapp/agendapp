package com.android.sample.ui.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepositoryInMemory
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.map.MapRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.CalendarScreenTestTags.DAY_HEADER_DAY_PREFIX
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Rule
import org.junit.Test

// Tests assisted by AI

/**
 * UI tests targeting only the wiring specific to **CalendarEventSelector**.
 *
 * We **inherit from [BaseCalendarScreenTest]** so we can reuse its utilities without
 * re-implementing them here: data builders (e.g. [buildTestEvents]), repository setup
 * ([populateRepo]), view-model factory & owner ([CalendarVMFactory], [TestOwner]), and interaction
 * helpers (e.g. [swipeLeft], [swipeRight], [assertEventVisible], [assertEventAbsent], [dowLabel]).
 * This keeps these tests focused on the Selector’s responsibilities instead of retesting the
 * internals of `CalendarContainer`, which is already covered by `CalendarScreen` tests.
 */
class CalendarEventSelectorTests : BaseCalendarScreenTest() {

  @get:Rule override val composeTestRule = createComposeRule()

  /**
   * Compose the **CalendarEventSelector** with an in-memory repository pre-populated with [events].
   *
   * Implementation notes:
   * - We reuse the helpers from the base class to build data, populate the repo, and provide a
   *   `ViewModelStoreOwner` whose default factory knows how to create our `CalendarViewModel`.
   * - This mirrors `setContentWithLocalRepo(...)` from the CalendarScreen tests but mounts the
   *   **Selector** instead of the full screen.
   */
  private fun setSelectorContentWithLocalRepo(events: List<Event> = buildTestEvents()) {
    val repoMap = MapRepositoryLocal()
    val repoEvents = EventRepositoryInMemory()
    populateRepo(repoEvents, events)
    val owner = TestOwner(CalendarVMFactory(repoEvents, repoMap))

    composeTestRule.setContent {
      CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        CalendarEventSelector(
            calendarViewModel = viewModel(factory = CalendarVMFactory(repoEvents, repoMap)))
      }
    }
  }

  @Test
  fun selector_composes_and_shows_core_tags() {
    // Purpose: sanity check that the Selector mounts and exposes the key structure tags
    setSelectorContentWithLocalRepo()

    // Root of the selector card/layout
    composeTestRule.onNodeWithTag(CalendarEventSelectorTestTags.SCREEN_ROOT).assertIsDisplayed()

    // Core parts coming from CalendarContainer (already tested elsewhere, here just existence)
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.EVENT_GRID).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TIME_AXIS_COLUMN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.DAY_ROW).assertIsDisplayed()
  }

  @Test
  fun currentWeek_eventsVisible_othersAbsent() {
    // Purpose: Only current-week events should be present initially; others absent.
    setSelectorContentWithLocalRepo()

    // Current week visible
    assertEventVisible("First Event")
    assertEventVisible("Nice Event")
    assertEventVisible("Top Event")

    // Other weeks: nodes should not exist in the tree
    assertEventAbsent("Next Event")
    assertEventAbsent("Later Event")
    assertEventAbsent("Previous Event")
    assertEventAbsent("Earlier Event")
  }

  @Test
  fun swipeLeft_showsNextWeek_then_backWithSwipeRight() {
    // Purpose: Verify that Selector navigation handlers advance and rewind the week range.
    setSelectorContentWithLocalRepo()

    // Baseline: current week
    assertEventVisible("First Event")
    assertEventAbsent("Next Event")

    // Go to next week
    swipeLeft()
    assertEventVisible("Next Event")
    assertEventVisible("Later Event")
    assertEventAbsent("First Event")
    assertEventAbsent("Top Event")

    // Come back
    swipeRight()
    assertEventVisible("First Event")
    assertEventVisible("Nice Event")
    assertEventVisible("Top Event")
  }

  @Test
  fun headers_match_week_then_update_after_swipe() {
    // Purpose: Day headers should reflect the currently displayed week and change after a swipe.
    setSelectorContentWithLocalRepo()

    val monday = LocalDate.now().with(DayOfWeek.MONDAY)

    // Headers for current week
    (0 until 5)
        .map { dowLabel(monday.plusDays(it.toLong())) }
        .forEachIndexed { index, label ->
          composeTestRule.onNodeWithTag("${DAY_HEADER_DAY_PREFIX}$index").assertIsDisplayed()
        }

    // After navigating to next week, headers should update accordingly
    swipeLeft()
    val nextMonday = monday.plusWeeks(1)
    (0 until 5)
        .map { dowLabel(nextMonday.plusDays(it.toLong())) }
        .forEachIndexed { index, label ->
          composeTestRule.onNodeWithTag("${DAY_HEADER_DAY_PREFIX}$index").assertIsDisplayed()
        }

    // And return to current week
    swipeRight()
    (0 until 5)
        .map { dowLabel(monday.plusDays(it.toLong())) }
        .forEachIndexed { index, label ->
          composeTestRule.onNodeWithTag("${DAY_HEADER_DAY_PREFIX}$index").assertIsDisplayed()
        }
  }

  @Test
  fun onlyEventsInSelectedOrganizationAreShown() {
    val otherOrgId = "otherOrg"

    // Events for the selected organization (orgTest)
    val allowedEvents =
        createEvent(
            organizationId = organizationId,
            title = "Visible Event 1",
            startDate = at(LocalDate.now(), LocalTime.of(10, 0)),
            endDate = at(LocalDate.now(), LocalTime.of(11, 0)),
            cloudStorageStatuses = emptySet(),
            participants = emptySet()) +
            createEvent(
                organizationId = organizationId,
                title = "Visible Event 2",
                startDate = at(LocalDate.now(), LocalTime.of(12, 0)),
                endDate = at(LocalDate.now(), LocalTime.of(13, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet())

    // Events belonging to another organization (should not appear)
    val forbiddenEvents =
        createEvent(
            organizationId = otherOrgId,
            title = "Hidden Event 1",
            startDate = at(LocalDate.now(), LocalTime.of(10, 0)),
            endDate = at(LocalDate.now(), LocalTime.of(11, 0)),
            cloudStorageStatuses = emptySet(),
            participants = emptySet()) +
            createEvent(
                organizationId = otherOrgId,
                title = "Hidden Event 2",
                startDate = at(LocalDate.now(), LocalTime.of(14, 0)),
                endDate = at(LocalDate.now(), LocalTime.of(15, 0)),
                cloudStorageStatuses = emptySet(),
                participants = emptySet())

    val repoMap = MapRepositoryLocal()
    val repoEvent = EventRepositoryInMemory()

    populateRepo(repoEvent, allowedEvents, organizationId)

    SelectedOrganizationRepository.changeSelectedOrganization(otherOrgId)
    populateRepo(repoEvent, forbiddenEvents, otherOrgId)

    SelectedOrganizationRepository.changeSelectedOrganization(organizationId)

    val owner = TestOwner(CalendarVMFactory(repoEvent, repoMap))
    composeTestRule.setContent {
      CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
        CalendarEventSelector(
            calendarViewModel = viewModel(factory = CalendarVMFactory(repoEvent, repoMap)))
      }
    }

    // Allowed events must appear
    assertEventVisible("Visible Event 1")
    assertEventVisible("Visible Event 2")

    // Forbidden events must NOT appear at all (node does not exist)
    assertEventAbsent("Hidden Event 1")
    assertEventAbsent("Hidden Event 2")
  }
}
