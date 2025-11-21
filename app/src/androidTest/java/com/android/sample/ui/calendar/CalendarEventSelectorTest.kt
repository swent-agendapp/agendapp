package com.android.sample.ui.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.ui.calendar.CalendarScreenTestTags.DAY_HEADER_DAY_PREFIX
import java.time.DayOfWeek
import java.time.LocalDate
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
    val repo = EventRepositoryLocal()
    populateRepo(repo, events)
    val owner = TestOwner(CalendarVMFactory(repo))

    composeTestRule.setContent {
      CompositionLocalProvider(LocalViewModelStoreOwner provides owner) { CalendarEventSelector() }
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
}
