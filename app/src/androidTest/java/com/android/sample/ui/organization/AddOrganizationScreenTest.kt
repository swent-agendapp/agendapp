package com.android.sample.ui.organization

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AddOrganizationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Composable
  fun TestScreen() {
    val fakeAddOrganizationViewModel = FakeAddOrganizationViewModel("Test Org")
    val fakeOrganizationViewModel = FakeOrganizationViewModel()

    AddOrganizationScreen(
        addOrganizationViewModel = fakeAddOrganizationViewModel,
        organizationViewModel = fakeOrganizationViewModel)
  }

  @Test
  fun screenIsDisplayed() {
    composeTestRule.setContent { TestScreen() }

    composeTestRule.onNodeWithTag(AddOrganizationScreenTestTags.ROOT).assertExists()
  }

  @Test
  fun textField_isDisplayed() {
    composeTestRule.setContent { TestScreen() }

    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.ORGANIZATION_NAME_TEXT_FIELD)
        .assertExists()
  }

  @Test
  fun buttons_areDisplayed() {
    composeTestRule.setContent { TestScreen() }

    composeTestRule.onNodeWithTag(AddOrganizationScreenTestTags.CREATE_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(AddOrganizationScreenTestTags.BACK_BUTTON).assertExists()
  }

  @Test
  fun buttons_areFunctional() {
    var backClicked = false
    var finishClicked = false

    composeTestRule.setContent {
      val fakeAddOrganizationViewModel = FakeAddOrganizationViewModel("Test Org")
      val fakeOrganizationViewModel = FakeOrganizationViewModel()

      AddOrganizationScreen(
          addOrganizationViewModel = fakeAddOrganizationViewModel,
          organizationViewModel = fakeOrganizationViewModel,
          onNavigateBack = { backClicked = true },
          onFinish = { finishClicked = true })
    }

    // Check the Back button works
    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.BACK_BUTTON)
        .assertExists()
        .assertIsEnabled()
        .performClick()

    // Check that the onNavigateBack lambda was called
    assert(backClicked) { "onNavigateBack lambda was not invoked" }

    // Check the Create button works
    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.CREATE_BUTTON)
        .assertExists()
        .assertIsEnabled()
        .performClick()

    // Check that the onFinish lambda was called
    assert(finishClicked) { "onFinish lambda was not invoked" }
  }
}
