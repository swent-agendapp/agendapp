package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.utils.FirebaseEmulatedTest
import java.time.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReplacementOrganizeFlowTest : FirebaseEmulatedTest() {

  @get:Rule val composeTestRule = createComposeRule()

  val selectedOrganizationId = "orgTest"

  private lateinit var fakeViewModel: ReplacementOrganizeViewModel

  @Before
  override fun setUp() {
    super.setUp()
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationId)
    fakeViewModel = ReplacementOrganizeViewModel()
    composeTestRule.setContent {
      ReplacementOrganizeScreen(
          replacementOrganizeViewModel = fakeViewModel,
          onCancel = {},
          onProcessNow = {},
          onProcessLater = {})
    }
  }

  @Test
  fun fullFlow_displaysCorrectScreensAndNavigates() {
    // STEP 1: SelectSubstitute
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.MEMBER_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText("alice@example.com").performClick()

    // Buttons should be enabled
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsEnabled()

    // Navigate to SelectEvents
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).performClick()
    composeTestRule
        .onNodeWithText("Select the events for which alice@example.com needs a replacement")
        .assertIsDisplayed()

    // Navigate back to SelectSubstitute
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.MEMBER_LIST).assertIsDisplayed()

    // Navigate to SelectDateRange
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .performClick()
    composeTestRule
        .onNodeWithText("Select the date range for which alice@example.com needs a replacement")
        .assertIsDisplayed()
    fakeViewModel.setStartInstant(Instant.parse("2024-01-05T00:00:00Z"))
    fakeViewModel.setEndInstant(Instant.parse("2024-01-01T00:00:00Z"))
    // Buttons should be disabled
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT)
        .assertIsDisplayed()

    fakeViewModel.setStartInstant(Instant.parse("2024-01-01T00:00:00Z"))
    fakeViewModel.setEndInstant(Instant.parse("2024-01-05T00:00:00Z"))
    // Buttons should be enabled
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT)
        .assertDoesNotExist()

    // Navigate to SelectProcessMoment
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.PROCESS_NOW_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.PROCESS_LATER_BUTTON)
        .assertIsDisplayed()
  }
}
