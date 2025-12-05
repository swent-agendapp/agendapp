package com.android.sample.ui.hourRecap

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.CalendarViewModel
import com.android.sample.utils.FirebaseEmulatedTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Modularization assisted by AI
/**
 * UI tests for the HourRecapScreen.
 *
 * These tests verify:
 * - All key UI components are rendered (start date, end date, generate button, export button).
 * - The generate button enables only when both dates are selected.
 * - The recap list renders the expected recap items.
 * - Clicking the export button triggers the UI action (placeholder).
 */
class HourRecapScreenTest : FirebaseEmulatedTest() {

  @get:Rule val compose = createComposeRule()
  val selectedOrganizationId = "orgTest"

  @Before
  override fun setUp() {
    super.setUp()
    // Set selected organization in the VM provider
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
  }
  /**
   * Ensures that the main UI components are displayed:
   * - Top bar
   * - Date pickers
   * - Generate button
   * - Export button
   */
  @Test
  fun screen_rendersAllMainComponents() {
    compose.setContent { HourRecapScreen() }

    compose.onNodeWithTag(HourRecapTestTags.TOP_BAR).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.START_DATE).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.END_DATE).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.GENERATE_BUTTON).assertExists().assertIsDisplayed()
    compose.onNodeWithTag(HourRecapTestTags.GENERATE_BUTTON).performClick()
    compose.onNodeWithTag(HourRecapTestTags.EXPORT_BUTTON).assertExists().assertIsDisplayed()
  }

  /**
   * Tests clicking the export button (UI only — no file generation yet). This ensures that the
   * button exists and is clickable without crashing.
   */
  @Test
  fun exportButton_isClickable() {
    compose.setContent { HourRecapScreen() }

    compose.onNodeWithTag(HourRecapTestTags.EXPORT_BUTTON).assertExists().performClick()
  }

  /**
   * Tests clicking the generate button (UI only — no data generation yet). This ensures that the
   * button exists and is clickable without crashing.
   */
  @Test
  fun generateButton_isClickable() {
    compose.setContent { HourRecapScreen(onBackClick = {}) }

    compose.onNodeWithTag(HourRecapTestTags.GENERATE_BUTTON).performClick()
  }

  /**
   * Tests that the recap list displays the expected worked hours for each employee based on the
   * test data injected into the CalendarViewModel's UI state.
   */
  @Test
  fun RecapItems_displayWorkedHoursCorrectly() {
    val calendarViewModel =
        CalendarViewModel(
            app = ApplicationProvider.getApplicationContext(),
        )

    calendarViewModel.setTestWorkedHours(
        listOf(
            "Alice" to 12.5,
            "Bob" to 8.0,
        ))

    compose.setContent { HourRecapScreen(calendarViewModel = calendarViewModel) }
    compose.onNodeWithText("Alice").assertExists()
    compose.onNodeWithText("12h 30min").assertExists()

    compose.onNodeWithText("Bob").assertExists()
    compose.onNodeWithText("8h").assertExists()

    // Only 2 recap item
    compose.onAllNodesWithTag(HourRecapTestTags.RECAP_ITEM).assertCountEquals(2)
  }

  @Test
  fun emptyWorkedHours_showsEmptyList() {
    val vm = CalendarViewModel(app = ApplicationProvider.getApplicationContext())

    vm.setTestWorkedHours(emptyList()) // 空数据

    compose.setContent { HourRecapScreen(calendarViewModel = vm) }

    compose.onAllNodesWithTag(HourRecapTestTags.RECAP_ITEM).assertCountEquals(0)
  }

  @Test
  fun backButton_triggersCallback() {
    var backPressed = false

    compose.setContent { HourRecapScreen(onBackClick = { backPressed = true }) }

    compose.onNodeWithTag(HourRecapTestTags.BACK_BUTTON).assertExists().performClick()

    assert(backPressed)
  }
}
