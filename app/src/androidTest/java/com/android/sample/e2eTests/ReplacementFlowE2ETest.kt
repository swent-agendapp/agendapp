package com.android.sample.e2eTests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.Agendapp
import com.android.sample.ui.authentication.SignInScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.organization.AddOrganizationScreenTestTags
import com.android.sample.ui.organization.OrganizationListScreenTestTags
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeListTestTags
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReplacementFlowE2ETest : FirebaseEmulatedTest() {

  // Timeout for UI authentication operations
  // This is used to wait for the UI to update after authentication actions
  companion object {
    private const val UI_AUTH_TIMEOUT = 10_000L
  }

  val eventTitle = "Event_Test"
  val eventDescription = "Event Test"
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
  fun signIn_createWeeklyEventTest_checkReplacement() {

    // Check that no user is signed in at start
    assert(FirebaseEmulator.auth.currentUser == null)

    // Launch app
    composeTestRule.setContent { Agendapp(credentialManager = fakeCredentialManager) }
    composeTestRule.waitForIdle()

    // Ensure Sign-In screen is displayed
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_TITLE).assertIsDisplayed()

    // Perform Sign-In
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait for sign-in to complete
    composeTestRule.waitUntil(timeoutMillis = UI_AUTH_TIMEOUT) {
      // Verify that Organization screen exists
      composeTestRule
          .onAllNodesWithTag(OrganizationListScreenTestTags.ROOT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Create Organization to proceed to Calendar

    // Click on Add Organization button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait for Add Organization screen to load
    composeTestRule.waitForIdle()

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

    // Wait for Calendar screen to load
    composeTestRule.waitForIdle()

    // Verify Calendar screen exists
    composeTestRule
        .onAllNodesWithTag(CalendarScreenTestTags.ROOT)
        .fetchSemanticsNodes()
        .isNotEmpty()

    // Check that a user is signed in after sign in
    assert(FirebaseEmulator.auth.currentUser != null)

    // Verify Calendar screen is displayed
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()

    // Go to Event Creation Assistant
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.ADD_EVENT_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait for Add Event screen to load
    composeTestRule.waitForIdle()

    // Fill First Form
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performTextInput(eventTitle)

    // Wait for text input to be processed
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .performTextInput(eventDescription)

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(expectedName).assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(BottomBarTestTags.ITEM_REPLACEMENT)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(ReplacementEmployeeListTestTags.ASK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule
        .onNodeWithTag(ReplacementEmployeeListTestTags.SELECT_EVENT_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }
}
