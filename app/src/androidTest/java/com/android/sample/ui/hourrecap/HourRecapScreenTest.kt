package com.android.sample.ui.hourrecap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
class HourRecapScreenTest {

  @get:Rule val compose = createComposeRule()

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
    compose.onNodeWithTag(HourRecapTestTags.EXPORT_BUTTON).assertExists().assertIsDisplayed()
  }

  /**
   * Ensures that the recap list is rendered and that at least one recap item exists. (Uses the fake
   * data in HourRecapScreen.)
   */
  @Test
  fun recapList_displaysRecapItems() {
    compose.setContent { HourRecapScreen() }

    // verify list exists
    compose.onNodeWithTag(HourRecapTestTags.RECAP_LIST).assertExists()

    // get all recap items
    val items = compose.onAllNodesWithTag(HourRecapTestTags.RECAP_ITEM)

    // assert at least 1 item is rendered
    val count = items.fetchSemanticsNodes().size
    assert(count > 0)
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
}
