package com.android.sample.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.network.FakeConnectivityChecker
import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkTestBase
import com.android.sample.utils.FirebaseEmulator
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInScreenTest : NetworkTestBase {
  @get:Rule val composeTestRule = createComposeRule()

  override val fakeChecker = FakeConnectivityChecker(state = true)
  override val networkRepo = NetworkStatusRepository(fakeChecker)

  @Before
  fun setUp() {
    setupNetworkTestBase()
    FirebaseEmulator.auth.signOut()

    composeTestRule.setContent { SignInScreen() }
  }

  @Test
  fun topTitleIsCorrectlySet() {
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

  @Test
  fun signInButtonDisabledWhenNoNetwork() {
    simulateNoInternet()

    // Check the button is displayed and disabled
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val expectedText = context.getString(R.string.network_error_message)

    // Check the text message is correct
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.NO_NETWORK_MESSAGE)
        .assertTextEquals(expectedText)
  }
}
