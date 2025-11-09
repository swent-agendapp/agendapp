package com.android.sample.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class AdminContactScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun adminContactScreen_displaysAdminInformation() {
    composeTestRule.setContent { AdminContactScreen(onNavigateBack = {}) }

    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT)
        .assertIsDisplayed()
        .assertTextContains(AdminInformation.EMAIL)
        .assertHasClickAction() // ensure it's clickable

    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT)
        .assertIsDisplayed()
        .assertTextContains(AdminInformation.PHONE)
        .assertHasClickAction() // ensure it's clickable
  }

  @Test
  fun adminContactScreen_backButtonWorks() {
    var backClicked = false
    composeTestRule.setContent { AdminContactScreen(onNavigateBack = { backClicked = true }) }

    composeTestRule.onNodeWithTag(AdminContactScreenTestTags.BACK_BUTTON).performClick()

    assert(backClicked)
  }

  @Test
  fun adminContactScreen_rootIsDisplayed() {
    composeTestRule.setContent { AdminContactScreen() }

    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE)
        .assertIsDisplayed()
  }

  @Test
  fun all_components_Displayed_andClickable() {
    composeTestRule.setContent { AdminContactScreen() }

    composeTestRule.onNodeWithTag(AdminContactScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT)
        .assertIsDisplayed()
        .assertHasClickAction()

    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT)
        .assertIsDisplayed()
        .assertHasClickAction()
  }
}
