package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.replacement.components.SelectDateRangeScreen
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class SelectDateRangeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun screenElements_areDisplayedCorrectly() {
    composeTestRule.setContent {
      SelectDateRangeScreen(
          onNext = {},
          onBack = {},
          title = "Title",
          instruction = "Instruction",
          onStartDateSelected = {},
          onEndDateSelected = {},
          canGoNext = true,
      )
    }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.START_DATE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.END_DATE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun next_button_disabled_when_end_date_is_not_strictly_after_start_date() {
    composeTestRule.setContent {
      SelectDateRangeScreen(
          onNext = {},
          onBack = {},
          title = "Title",
          instruction = "Instruction",
          onStartDateSelected = {},
          onEndDateSelected = {},
          errorMessage = "Invalid range",
          canGoNext = false,
      )
    }

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT)
        .assertIsDisplayed()
  }

  @Test
  fun next_button_enabled_calls_onNext() {
    var nextCalled = false

    composeTestRule.setContent {
      SelectDateRangeScreen(
          onNext = { nextCalled = true },
          onBack = {},
          title = "Title",
          instruction = "Instruction",
          onStartDateSelected = {},
          onEndDateSelected = {},
          canGoNext = true,
      )
    }

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON)
        .assertIsDisplayed()
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitForIdle()

    assert(nextCalled)
  }

  @Test
  fun back_button_calls_onBack() {
    var backCalled = false

    composeTestRule.setContent {
      SelectDateRangeScreen(
          onNext = {},
          onBack = { backCalled = true },
          title = "Title",
          instruction = "Instruction",
          onStartDateSelected = {},
          onEndDateSelected = {},
          canGoNext = true,
      )
    }

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()

    assert(backCalled)
  }

  @Test
  fun error_container_visible_without_message_when_errorMessage_is_null_and_cannot_go_next() {
    composeTestRule.setContent {
      SelectDateRangeScreen(
          onNext = {},
          onBack = {},
          title = "Title",
          instruction = "Instruction",
          onStartDateSelected = {},
          onEndDateSelected = {},
          errorMessage = null,
          canGoNext = false,
      )
    }

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT)
        .assertExists()
  }
}
