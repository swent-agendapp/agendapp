package com.android.sample.ui.navigation

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.android.sample.Agendapp
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.replacement.ReplacementTestTags
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import com.github.se.bootcamp.ui.authentication.SignInScreenTestTags
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [Agendapp] navigation. This test checks the navigation flow between Home,
 * EditEvent, Calendar, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest : FirebaseEmulatedTest() {

  // Create a fake Google ID token for testing
  val fakeGoogleIdToken =
      FakeJwtGenerator.createFakeGoogleIdToken("login_test", "12345", email = "test@example.com")

  // Create a FakeCredentialManager with the fake token
  val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    // Ensure a user is signed in before each test (use runBlocking to call suspend function)
    runBlocking { FirebaseEmulator.signInWithFakeGoogleUser(fakeGoogleIdToken) }
  }

  @Test
  fun ensure_sign_in_screen_if_not_signed_in() = runTest {
    // Sign out user before launching app
    FirebaseEmulator.auth.signOut()

    // Launch app
    composeTestRule.setContent { Agendapp(credentialManager = fakeCredentialManager) }

    // Ensure Sign-In screen is displayed
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_MESSAGE).assertIsDisplayed()

    // Perform Sign-In
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun ensure_calendar_if_signed_in() = runTest {

    // Launch app with user already signed in
    composeTestRule.setContent { Agendapp() }

    // Verify Calendar screen is displayed (user remains signed in)
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
  }

  @Test
  fun navigate_to_replacement() {
    composeTestRule.setContent { Agendapp() }

    // Go to replacement
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_REPLACEMENT).assertExists().performClick()

    // Validate screen content
    composeTestRule.onNodeWithTag(ReplacementTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun addEventAndResetsTheFieldsTheNextTime() {
    composeTestRule.setContent { Agendapp() }

    // Go to add event screen
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertExists().performClick()

    // Validate screen content
    // Enter title and description
    composeTestRule
        .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Event")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Description")
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    // No recurrence end field for one time events
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertDoesNotExist()
    // Enter weekly recurrence
    composeTestRule
        .onNodeWithTag(AddEventTestTags.RECURRENCE_STATUS_DROPDOWN)
        .assertExists()
        .performClick()
    composeTestRule
        .onNodeWithTag(AddEventTestTags.recurrenceTag(RecurrenceStatus.Weekly))
        .assertExists()
        .performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    // Create event without any assignees
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertExists().performClick()
    // Finish screen
    composeTestRule.onNodeWithTag(AddEventTestTags.FINISH_BUTTON).assertExists().performClick()

    // Back to calendar screen
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertExists().performClick()

    // Validate that the fields are reset when adding a new event
    composeTestRule
        .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
        .assertExists()
        .assertTextContains("")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .assertExists()
        .assertTextContains("")
  }
}
