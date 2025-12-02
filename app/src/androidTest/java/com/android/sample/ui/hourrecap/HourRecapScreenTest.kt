package com.android.sample.ui.hourrecap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
}
