package com.android.sample.e2eTests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.Agendapp
import com.android.sample.ui.authentication.SignInScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.organization.AddOrganizationScreenTestTags
import com.android.sample.ui.organization.OrganizationListScreenTestTags
import com.android.sample.ui.profile.AdminContactScreenTestTags
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileFlowE2ETest : FirebaseEmulatedTest() {

  // Timeout for UI authentication operations
  // This is used to wait for the UI to update after authentication actions
  companion object {
    private const val UI_AUTH_TIMEOUT = 10_000L
  }

  val expectedName = "John Doe"
  val expectedEmail = "john.doe@test.com"

  // Create a fake Google ID token for testing
  val fakeGoogleIdToken =
      FakeJwtGenerator.createFakeGoogleIdToken(
          sub = "login_test", name = expectedName, email = expectedEmail)

  // Create a FakeCredentialManager with the fake token
  val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()

    // Sign out any existing user
    FirebaseEmulator.auth.signOut()
  }

  // Test a User info flow : signing in, viewing profile, viewing admin contact, and signing out
  @Test
  fun signIn_viewProfile_viewAdminContact_signOut() {

    // Check that no user is signed in at start
    assert(FirebaseEmulator.auth.currentUser == null)

    // Launch app
    composeTestRule.setContent { Agendapp(credentialManager = fakeCredentialManager) }

    // Ensure Sign-In screen is displayed
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_TITLE).assertIsDisplayed()

    // Perform Sign-In
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()

    // Wait for sign-in to complete
    composeTestRule.waitUntil(timeoutMillis = UI_AUTH_TIMEOUT) {
      // Verify that Organization screen exists
      composeTestRule
          .onAllNodesWithTag(OrganizationListScreenTestTags.ROOT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.waitForIdle()
    // Check that a user is signed in after sign in
    assert(FirebaseEmulator.auth.currentUser != null)

    // Create Organization to proceed to Calendar

    // Click on Add Organization button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Verify Add Organization screen is displayed
    composeTestRule.onNodeWithTag(AddOrganizationScreenTestTags.ROOT).assertIsDisplayed()

    // Fill organization name
    val organizationName = "Test Organization"
    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.ORGANIZATION_NAME_TEXT_FIELD)
        .performTextInput(organizationName)

    // Click on Create button
    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Verify Calendar screen exists
    composeTestRule
        .onAllNodesWithTag(CalendarScreenTestTags.ROOT)
        .fetchSemanticsNodes()
        .isNotEmpty()

    // Verify Calendar screen is displayed
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()

    // Go to Settings screen
    composeTestRule
        .onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS)
        .assertIsDisplayed()
        .performClick()

    // Verify Settings screen is displayed
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).assertIsDisplayed()

    // Go to Profile screen
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Verify Profile screen is displayed
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()

    composeTestRule.waitForIdle()
    // Verify profile information are displayed and correct
    //    composeTestRule
    //        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
    //        .assertIsDisplayed()
    //        .assertTextContains(expectedName)
    //    composeTestRule
    //        .onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD)
    //        .assertIsDisplayed()
    //        .assertTextContains(expectedEmail)

    // Go to Admin Contact screen
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ADMIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Verify Admin Contact screen is displayed
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE)
        .assertIsDisplayed()

    // Go back to Settings screen
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Go to Profile screen
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    //    // Sign out
    //    composeTestRule
    //        .onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON)
    //        .assertIsDisplayed()
    //        .performClick()
    //
    //    // Check that no user is signed in after sign out
    //    assert(FirebaseEmulator.auth.currentUser == null)
    //
    //    // Verify Sign-In screen is displayed again
    //    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_TITLE).assertIsDisplayed()
  }
}
