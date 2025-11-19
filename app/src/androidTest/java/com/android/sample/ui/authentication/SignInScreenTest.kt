package com.android.sample.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.utils.FirebaseEmulator
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    FirebaseEmulator.auth.signOut()
  }

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
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGOUT_BUTTON).assertIsNotDisplayed()
  }
}
