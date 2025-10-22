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

    composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ADMIN_CONTACT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT)
        .assertIsDisplayed()
        .assertTextContains("admin@agendapp.com")
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT)
        .assertIsDisplayed()
        .assertTextContains("+1 (555) 123-4567")
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
    composeTestRule.setContent { AdminContactScreen(onNavigateBack = {}) }

    composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ROOT).assertIsDisplayed()
  }
}
