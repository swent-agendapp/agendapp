package com.android.sample.ui.calendar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.replacementEmployee.ReplacementEmployeeFlow
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementEmployeeCreateTestTags
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementEmployeeListTestTags
import org.junit.Rule
import org.junit.Test

// Assisted by AI
/**
 * UI tests for the **ReplacementEmployeeFlow**.
 *
 * These tests verify the correct navigation between the different steps of the employee-side
 * replacement workflow:
 * 1. **LIST** → The employee sees incoming replacement requests.
 * 2. **CREATE_OPTIONS** → The employee chooses how to create a replacement (select event / date
 *    range).
 * 3. **SELECT_EVENT** → The employee selects a specific event to request replacement for.
 * 4. **SELECT_DATE_RANGE** → The employee selects a date range to generate multiple replacements.
 *
 * Each test asserts that:
 * - The expected UI components are visible
 * - Navigation occurs correctly when buttons are clicked
 * - The flow returns to the previous screens when back navigation is used
 *
 * All UI elements are located using test tags to ensure stable, idempotent UI tests.
 */
class ReplacementEmployeeFlowTest {

  @get:Rule val compose = createComposeRule()

  /**
   * Verifies that the **List screen** is shown initially, and that the "Ask to be replaced" button
   * is visible.
   *
   * This confirms:
   * - Default step is `LIST`
   * - ReplacementEmployeeListScreen is displayed
   */
  @Test
  fun listScreen_showsAskButton() {
    compose.setContent { ReplacementEmployeeFlow() }

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).assertExists()
  }

  /**
   * Ensures that clicking the "Ask to be replaced" button correctly navigates to the **Create
   * Options** screen.
   *
   * Expected result:
   * - SELECT_EVENT_BUTTON is visible
   */
  @Test
  fun clickingAskButton_goesToCreateOptions() {
    compose.setContent { ReplacementEmployeeFlow() }

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()

    compose.onNodeWithTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON).assertExists()
  }

  /**
   * Ensures that clicking: List → Ask → Select Event correctly navigates to the **Select Event**
   * screen.
   *
   * Validation:
   * - The screen should contain a "Next" button from SelectEventScreen
   */
  @Test
  fun selectEventButton_goesToSelectEventScreen() {
    compose.setContent { ReplacementEmployeeFlow() }

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()
    compose.onNodeWithTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON).performClick()

    compose.onNodeWithText("Next").assertExists()
  }

  /**
   * Tests the **back navigation** from the Select Event screen.
   *
   * Flow: List → Ask → Select Event → Back
   *
   * Expected:
   * - Return to the Create Options screen (SELECT_EVENT_BUTTON should exist again)
   */
  @Test
  fun backFromSelectEvent_returnsToCreateOptions() {
    compose.setContent { ReplacementEmployeeFlow() }

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()
    compose.onNodeWithTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON).performClick()

    compose.onNodeWithText("Go Back").performClick()

    compose.onNodeWithTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON).assertExists()
  }

  /**
   * Ensures that choosing the "Date range" option properly navigates to the **Select Date Range**
   * screen.
   *
   * Validation:
   * - Start date picker ("Start date") should be visible
   */
  @Test
  fun chooseDateRange_goesToDateRangeScreen() {
    compose.setContent { ReplacementEmployeeFlow() }

    compose.onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON).performClick()
    compose.onNodeWithTag(ReplacementEmployeeCreateTestTags.CHOOSE_DATE_RANGE_BUTTON).performClick()

    compose.onNodeWithText("Start date").assertExists() // from DatePickerField
  }
}
