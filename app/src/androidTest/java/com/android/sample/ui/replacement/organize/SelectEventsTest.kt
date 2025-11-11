package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.replacement.organize.components.SelectEventScreen
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class SelectEventsTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun screenElements_areDisplayedCorrectly() {
    composeTestRule.setContent { SelectEventScreen() }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun nextButton_isDisabled_whenNoEventSelected() {
    composeTestRule.setContent { SelectEventScreen() }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsNotEnabled()
  }
}
