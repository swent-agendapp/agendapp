package com.android.sample.ui.authentication

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.android.sample.utils.FailCredentialManager
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthentificationTest : FirebaseEmulatedTest() {
  val UI_AUTH_WAIT_TIMEOUT = 5_000L

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()
  }

  @Test
  fun google_sign_in_is_configured() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val resourceId =
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)

    // Skip test if resource doesn't exist (useful for CI environments)
    assumeTrue("Google Sign-In not configured - skipping test", resourceId != 0)

    val clientId = context.getString(resourceId)
    assertTrue(
        "Invalid Google client ID format: $clientId", clientId.endsWith(".googleusercontent.com"))
  }

  @Test
  fun loggedOut_showsSignInScreen() {
    // Ensure we are logged out
    assert(FirebaseEmulator.auth.currentUser == null)

    // Set the content to the full app
    composeTestRule.setContent { SignInScreen() }

    // Check that the sign-in screen is displayed
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun canSignInWithGoogle() {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("login_test", "12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)
    var isSignIn = false

    composeTestRule.setContent {
      SignInScreen(credentialManager = fakeCredentialManager, onSignedIn = { isSignIn = true })
    }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) { isSignIn }
    assertTrue(isSignIn)
  }

  @Test
  fun signOutFailWithGoogle() {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("login_test", "12345", email = "test@example.com")

    val failCredentialManager = FailCredentialManager.create(fakeGoogleIdToken)

    composeTestRule.setContent { SignInScreen(credentialManager = failCredentialManager) }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(UI_AUTH_WAIT_TIMEOUT) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.END_SNACK_BAR).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun signInWithGoogleFailDueToCredential() {
    val fakeGoogleIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken("login_test", "12345", email = "test@example.com")

    val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken, "Not valid")

    composeTestRule.setContent { SignInScreen(credentialManager = fakeCredentialManager) }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(UI_AUTH_WAIT_TIMEOUT) {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.END_SNACK_BAR).isDisplayed()
    }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.END_SNACK_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGOUT_BUTTON).assertIsNotDisplayed()
  }
}
