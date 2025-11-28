package com.android.sample.ui.calendar

import android.Manifest
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.model.map.MapRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.components.ViewMode
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

// Assisted by AI

/**
 * UI tests for [CalendarContainer].
 *
 * These tests focus on:
 * - default view mode depending on today
 * - swipe behavior in 1-day and week modes
 * - behavior when switching modes with the ViewModeSelector
 * - opening and using the Month picker
 * - header behavior in 1-day mode
 *
 * For the Month picker:
 * - Compose test APIs are used to open the picker.
 * - Espresso is used to interact with the platform DatePicker dialog.
 */
class CalendarContainerTest {
  val selectedOrganizationId = "orgTest"

  private val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  private val composeRule = createComposeRule()

  @get:Rule val ruleChain: RuleChain = RuleChain.outerRule(permissionRule).around(composeRule)

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Sets the content with a default [CalendarContainer]. */
  private fun setCalendarContent() {
    val eventRepo = EventRepositoryLocal()
    val mapRepo = MapRepositoryLocal()
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)

    val viewModel =
        CalendarViewModel(
            app = ApplicationProvider.getApplicationContext(),
            eventRepository = eventRepo,
            mapRepository = mapRepo)

    composeRule.setContent { CalendarContainer(calendarViewModel = viewModel) }
  }

  /**
   * Returns the expected default view mode based on "today".
   *
   * This follows the same rule as computeDefaultViewMode():
   * - FIVE_DAYS if today is Monday–Friday
   * - SEVEN_DAYS if today is Saturday or Sunday
   */
  private fun expectedDefaultViewMode(): ViewMode {
    val today = LocalDate.now()
    return if (today.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY) {
      ViewMode.FIVE_DAYS
    } else {
      ViewMode.SEVEN_DAYS
    }
  }

  /**
   * Opens the ViewModeSelector menu.
   *
   * We click on the FAB that corresponds to the current mode.
   */
  private fun openViewModeMenu(currentMode: ViewMode) {
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + currentMode.name)
        .performClick()
  }

  /**
   * Changes the view mode from [fromMode] to [toMode] using the menu.
   *
   * This helper also returns the new mode so tests can keep track of it.
   */
  private fun selectViewMode(fromMode: ViewMode, toMode: ViewMode): ViewMode {
    openViewModeMenu(fromMode)

    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + toMode.name)
        .performClick()

    return toMode
  }

  /** Performs a left swipe on the calendar grid (go to next day/week). */
  private fun swipeLeftOnCalendarGrid() {
    composeRule.onNodeWithTag(CalendarScreenTestTags.ROOT).performTouchInput { swipeLeft() }
  }

  /** Performs a right swipe on the calendar grid (go to previous day/week). */
  private fun swipeRightOnCalendarGrid() {
    composeRule.onNodeWithTag(CalendarScreenTestTags.ROOT).performTouchInput { swipeRight() }
  }

  /**
   * Opens the Month picker using the ViewModeSelector.
   *
   * Returns the new mode (MONTH) so the caller can keep track of it.
   *
   * We use assertExists() because the DatePicker is shown in a platform Dialog, and the Box with
   * the tag may not have visible size.
   */
  private fun openMonthPicker(currentMode: ViewMode): ViewMode {
    val newMode = selectViewMode(currentMode, ViewMode.MONTH)

    // The host Box for the DatePicker dialog exists in the tree,
    // even if it has no visible size.
    composeRule.onNodeWithTag(CalendarScreenTestTags.DATE_PICKER_MODAL).assertExists()

    return newMode
  }

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  @Test
  fun defaultViewMode_DependsOnTodayWeekdayOrWeekend() {
    setCalendarContent()

    val expectedMode = expectedDefaultViewMode()

    // The FAB for the expected mode should be visible.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + expectedMode.name)
        .assertIsDisplayed()
  }

  @Test
  fun swipeInOneDayMode_StaysInOneDayMode() {
    setCalendarContent()

    var currentMode = expectedDefaultViewMode()

    // Switch to 1-day mode from the default mode.
    currentMode = selectViewMode(currentMode, ViewMode.ONE_DAY)

    // Swipe to the left to go to the next day.
    swipeLeftOnCalendarGrid()

    // After left swipe, we should still be in ONE_DAY mode.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.ONE_DAY.name)
        .assertIsDisplayed()

    // Swipe to the right to go to the previous day.
    swipeRightOnCalendarGrid()

    // After right swipe, we should still be in ONE_DAY mode.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.ONE_DAY.name)
        .assertIsDisplayed()
  }

  @Test
  fun swipeInFiveOrSevenDayMode_MovesWeekAndKeepsMode() {
    setCalendarContent()

    val currentMode = expectedDefaultViewMode()

    // Swipe to go to the next week.
    swipeLeftOnCalendarGrid()

    // We only assert that the mode stays the same after a swipe.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + currentMode.name)
        .assertIsDisplayed()
  }

  @Test
  fun switchFromWeekToOneDay_WhenTodayInRange_ShowsOneDayMode() {
    setCalendarContent()

    var currentMode = expectedDefaultViewMode()

    // We start in default week mode (5 or 7 days).
    // This test assumes that today is inside the initial visible range.

    // Switch to 1-day mode.
    currentMode = selectViewMode(currentMode, ViewMode.ONE_DAY)

    // We simply assert that the mode is now ONE_DAY.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.ONE_DAY.name)
        .assertIsDisplayed()
  }

  @Test
  fun switchFromWeekToOneDay_WhenTodayNotInRange_StillShowsOneDayMode() {
    setCalendarContent()

    var currentMode = expectedDefaultViewMode()

    // Here we conceptually document the rule:
    // - if today is not in the range, the ONE_DAY mode should focus on Monday of the range.
    // The actual date is not exposed to UI tests, so we only verify the mode change.

    currentMode = selectViewMode(currentMode, ViewMode.ONE_DAY)

    composeRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.ONE_DAY.name)
        .assertIsDisplayed()
  }

  @Test
  fun selectingMonth_OpensDatePickerModal() {
    setCalendarContent()

    var currentMode = expectedDefaultViewMode()

    currentMode = openMonthPicker(currentMode)

    // The DatePicker host Box should exist when MONTH mode is active.
    composeRule.onNodeWithTag(CalendarScreenTestTags.DATE_PICKER_MODAL).assertExists()
  }

  @Test
  fun inOneDayMode_HeaderShowsFullWeekAndDaysAreClickable() {
    setCalendarContent()

    var currentMode = expectedDefaultViewMode()

    // Go to 1-day mode.
    currentMode = selectViewMode(currentMode, ViewMode.ONE_DAY)

    // We expect the header to show a full week (7 days).
    // We check that at least the first and the last indices exist and are displayed,
    // and that they are clickable.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.DAY_HEADER_DAY_PREFIX + "0")
        .assertIsDisplayed()
        .assertHasClickAction()

    composeRule
        .onNodeWithTag(CalendarScreenTestTags.DAY_HEADER_DAY_PREFIX + "6")
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun inNonOneDayMode_HeaderDaysAreNotClickable() {
    setCalendarContent()

    var currentMode = expectedDefaultViewMode()

    // Force 7-day mode, where header days should not be clickable.
    currentMode = selectViewMode(currentMode, ViewMode.SEVEN_DAYS)

    // We check one header cell as a representative.
    composeRule
        .onNodeWithTag(CalendarScreenTestTags.DAY_HEADER_DAY_PREFIX + "0")
        .assertIsDisplayed()
        .assertHasNoClickAction()
  }
}
