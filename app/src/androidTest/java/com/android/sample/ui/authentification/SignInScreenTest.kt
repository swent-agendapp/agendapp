package com.android.sample.ui.authentification

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class SignInScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topTitleIsCorrectlySet() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.APP_LOGO).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_TITLE)
        .assertIsDisplayed()
        .assertTextEquals("Agendapp")
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_MESSAGE)
        .assertIsDisplayed()
        .assertTextEquals("Plan, track and manage")
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_WELCOME)
        .assertIsDisplayed()
        .assertTextEquals("Welcome")
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }
}
