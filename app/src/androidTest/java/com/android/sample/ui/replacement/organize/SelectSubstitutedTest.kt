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
          onMemberSelected = { fakeViewModel.setSelectedMember(it) },
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
    // Click on U3
    composeTestRule.onNodeWithText("U3").performClick()

    // Verify selected member
    assert(fakeViewModel.uiState.value.selectedMember?.id == "U3")

    // Buttons should be enabled
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsEnabled()
  }

  @Test
  fun searchFilter_filtersList() {
    // Type "Ali" in search bar
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("2")

    composeTestRule.onNodeWithText("U2").assertIsDisplayed()
    composeTestRule.onNodeWithText("U1").assertDoesNotExist()
    composeTestRule.onNodeWithText("U3").assertDoesNotExist()
    composeTestRule.onNodeWithText("U4").assertDoesNotExist()
  }
}
