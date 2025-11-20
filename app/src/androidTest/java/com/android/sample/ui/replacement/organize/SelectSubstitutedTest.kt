package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.ui.replacement.organize.components.SelectSubstitutedScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectSubstitutedScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var fakeViewModel: ReplacementOrganizeViewModel

  @Before
  fun setUp() {
    fakeViewModel = ReplacementOrganizeViewModel()
    fakeViewModel.loadOrganizationMembers()
    composeTestRule.setContent {
      SelectSubstitutedScreen(
          replacementOrganizeViewModel = fakeViewModel,
          onSelectEvents = {},
          onSelectDateRange = {},
          onBack = {})
    }
  }

  @Test
  fun screenElements_areDisplayedCorrectly() {
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
  fun buttons_areDisabled_whenNoMemberSelected() {
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON)
        .assertIsNotEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun memberSelection_enablesButtons() {
    // Click on charlie's email
    composeTestRule.onNodeWithText("charlie@example.com").performClick()

    // Verify selected member
    assert(fakeViewModel.uiState.value.selectedMember?.email == "charlie@example.com")

    // Buttons should be enabled
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsEnabled()
  }

  @Test
  fun searchFilter_filtersList_with_email() {
    composeTestRule.onNodeWithText("alice@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("bob@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("charlie@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("dana@example.com").assertIsDisplayed()

    // Type "ali" in search bar
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("ali")

    composeTestRule.onNodeWithText("alice@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("bob@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("charlie@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("dana@example.com").assertDoesNotExist()
  }

  @Test
  fun searchFilter_filtersList_with_id() {
    composeTestRule.onNodeWithText("alice@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("bob@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("charlie@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("dana@example.com").assertIsDisplayed()

    // Type "U4" in search bar
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("U4")

    composeTestRule.onNodeWithText("alice@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("bob@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("charlie@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("dana@example.com").assertIsDisplayed()
  }

  @Test
  fun searchFilter_filterList_with_displayName() {
    composeTestRule.onNodeWithText("alice@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("bob@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("charlie@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("dana@example.com").assertIsDisplayed()

    // Type "bob boss" in search bar
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("bob boss")

    composeTestRule.onNodeWithText("alice@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("bob@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("charlie@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("dana@example.com").assertDoesNotExist()
  }

  @Test
  fun searchFilter_filtersList_noMatch() {
    composeTestRule.onNodeWithText("alice@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("bob@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("charlie@example.com").assertIsDisplayed()
    composeTestRule.onNodeWithText("dana@example.com").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("unknown user")

    composeTestRule.onNodeWithText("dana@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("alice@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("bob@example.com").assertDoesNotExist()
    composeTestRule.onNodeWithText("charlie@example.com").assertDoesNotExist()
  }
}
