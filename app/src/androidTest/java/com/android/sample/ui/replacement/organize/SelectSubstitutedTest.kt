package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.ui.replacement.organize.components.SelectSubstitutedScreen
import org.junit.Rule
import org.junit.Test

// Assisted by AI
class SelectSubstitutedTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun screenElements_areDisplayedCorrectly() {
    composeTestRule.setContent { SelectSubstitutedScreen() }

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.MEMBER_LIST).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECTED_MEMBER_INFO)
        .assertIsDisplayed()
  }

  @Test
  fun memberSelection_enablesButtons() {
    var selectedMember = ""

    composeTestRule.setContent {
      SelectSubstitutedScreen(
          onMemberSelected = { selectedMember = it },
          onSelectEvents = {},
          onSelectDateRange = {},
          onBack = {})
    }

    // Click on "Alice"
    composeTestRule.onNodeWithText("Alice").performClick()

    // Verify callbacks
    assert(selectedMember == "Alice")

    // Buttons should be enabled
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsEnabled()
  }

  @Test
  fun searchFilter_filtersList() {
    composeTestRule.setContent { SelectSubstitutedScreen() }

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("Ali")

    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob").assertDoesNotExist()
  }
}
