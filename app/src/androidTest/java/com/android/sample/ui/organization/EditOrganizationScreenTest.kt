package com.android.sample.ui.organization

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class EditOrganizationScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Composable
  fun TestScreen() {
    EditOrganizationScreen()
  }

  @Test
  fun screenIsDisplayed() {
    composeTestRule.setContent { TestScreen() }

    composeTestRule.onNodeWithTag(EditOrganizationTestTags.ROOT).assertExists()
  }

  @Test
  fun textField_isDisplayed() {
    composeTestRule.setContent { TestScreen() }

    composeTestRule
        .onNodeWithTag(EditOrganizationTestTags.NEW_ORGANIZATION_NAME_TEXT_FIELD)
        .assertExists()
  }

  @Test
  fun buttons_areDisplayed() {
    composeTestRule.setContent { TestScreen() }

    composeTestRule.onNodeWithTag(EditOrganizationTestTags.EDIT_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(EditOrganizationTestTags.BACK_BUTTON).assertExists()
  }

  @Test
  fun buttons_areFunctional() {
    var backClicked = false
    var finishClicked = false

    composeTestRule.setContent {
      EditOrganizationScreen(
          onNavigateBack = { backClicked = true }, onFinish = { finishClicked = true })
    }

    // Fill in the organization name to enable the Create button
    composeTestRule
        .onNodeWithTag(EditOrganizationTestTags.NEW_ORGANIZATION_NAME_TEXT_FIELD)
        .performTextInput("Edited Organization")

    // Check the Back button works
    composeTestRule
        .onNodeWithTag(EditOrganizationTestTags.BACK_BUTTON)
        .assertExists()
        .assertIsEnabled()
        .performClick()

    // Check that the onNavigateBack lambda was called
    assert(backClicked) { "onNavigateBack lambda was not invoked" }

    // Check the Create button works
    composeTestRule
        .onNodeWithTag(EditOrganizationTestTags.EDIT_BUTTON)
        .assertExists()
        .assertIsEnabled()

    composeTestRule.onNodeWithTag(EditOrganizationTestTags.EDIT_BUTTON).performClick()

    assert(finishClicked)
  }
}
