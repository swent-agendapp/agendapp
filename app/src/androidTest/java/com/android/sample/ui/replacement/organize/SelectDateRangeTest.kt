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
    composeTestRule.setContent { SelectDateRangeScreen() }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.START_DATE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.END_DATE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun next_button_disabled_when_end_date_is_not_strictly_after_start_date() {
    composeTestRule.setContent { SelectDateRangeScreen() }

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT)
        .assertIsDisplayed()
  }
}
