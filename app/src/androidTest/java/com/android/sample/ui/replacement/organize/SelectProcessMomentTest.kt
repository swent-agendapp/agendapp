package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.replacement.organize.components.SelectProcessMomentScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class SelectProcessMomentTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun screenElements_areDisplayedCorrectly() {
    composeTestRule.setContent { SelectProcessMomentScreen() }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.PROCESS_NOW_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.PROCESS_LATER_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun clickingProcessNow_callsCallback() {
    var invoked = false

    composeTestRule.setContent { SelectProcessMomentScreen(onProcessNow = { invoked = true }) }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.PROCESS_NOW_BUTTON).performClick()
    assertTrue(invoked)
  }

  @Test
  fun clickingProcessLater_callsCallback() {
    var invoked = false

    composeTestRule.setContent { SelectProcessMomentScreen(onProcessLater = { invoked = true }) }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.PROCESS_LATER_BUTTON).performClick()
    assertTrue(invoked)
  }
}
