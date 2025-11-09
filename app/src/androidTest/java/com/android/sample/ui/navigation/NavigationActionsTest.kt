package com.android.sample.ui.navigation

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.android.sample.Agendapp
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.eventOverview.EventOverviewScreenTestTags
import com.android.sample.ui.replacement.ReplacementTestTags
import com.android.sample.ui.screens.HomeTestTags
import com.android.sample.ui.screens.HomeTestTags.CALENDAR_BUTTON
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

  // Timeout for UI authentication operations
  // This is used to wait for the UI to update after authentication actions
  val uiAuthWaitTimeOut = 10_000L

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
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Wait for sign-in to complete
    composeTestRule.waitUntil(timeoutMillis = uiAuthWaitTimeOut) {
      // Verify Home screen is displayed
      composeTestRule.onNodeWithTag(CALENDAR_BUTTON).isDisplayed()
    }
  }

  @Test
  fun ensure_home_if_signed_in() = runTest {

    // Launch app with user already signed in
    composeTestRule.setContent { Agendapp() }

    // Verify Home screen is still displayed (user remains signed in)
    composeTestRule.onNodeWithTag(CALENDAR_BUTTON).assertIsDisplayed()
  }

  /**
   * Scrolls vertically through the calendar until the node with [tag] is visible and can be
   * interacted with.
   */
  private fun scrollUntilVisible(tag: String, maxAttempts: Int = 2) {
    // The node should exist in the semantics tree, if it doesn't, test data/setup is wrong.
    composeTestRule.onNodeWithTag(tag).assertExists()

    repeat(maxAttempts) { attempt ->
      val displayed =
          runCatching {
                composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
                true
              }
              .getOrElse { false }

      if (displayed) return

      // Perform a gentle swipe up to reveal later hours small deltas reduce flakiness.
      composeTestRule.onRoot().performTouchInput { swipeUp() }

      composeTestRule.waitForIdle()
    }

    // Final assert to fail with a clear message if nothing became visible.
    composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
  }

  @Test
  fun navigate_to_all_add_forms() {
    composeTestRule.setContent { Agendapp() }

    // Go to Calendar
    composeTestRule.onNodeWithTag(CALENDAR_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertExists().performClick()
    // Validate screen content
    composeTestRule
        .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Event")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Description")
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.FINISH_BUTTON).assertExists().performClick()

    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun navigate_to_replacement() {
    composeTestRule.setContent { Agendapp() }

    // Go to replacement
    composeTestRule.onNodeWithTag(HomeTestTags.REPLACEMENT_BUTTON).assertExists().performClick()

    // Validate screen content
    composeTestRule.onNodeWithTag(ReplacementTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun addEventAndResetsTheFieldsTheNextTime() {
    composeTestRule.setContent { Agendapp() }

    // Go to add event screen
    composeTestRule.onNodeWithTag(CALENDAR_BUTTON).assertExists().performClick()
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

  /**
   * Flow of this test : -> Home -> Calendar -> Add an Event -> Calendar -> (click on the event) ->
   * EventOverview -> (click on GoBack) -> Calendar
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun navigate_calendar_to_eventOverview_and_back() {
    composeTestRule.setContent { Agendapp() }

    // Go to Calendar
    composeTestRule.onNodeWithTag(CALENDAR_BUTTON).assertExists().performClick()

    // Create a simple event using the real Add‑Event flow so that the calendar has at least one
    // event
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertExists().performClick()
    composeTestRule
        .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Event")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Description")
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.FINISH_BUTTON).assertExists().performClick()

    // Wait for at least one event block to be present, then click it
    composeTestRule.waitUntilAtLeastOneExists(
        hasTestTag("${CalendarScreenTestTags.EVENT_BLOCK}_Test Event"))
    // Small idle to avoid "failed to inject touch input" on emulator
    composeTestRule.waitForIdle()
    // Ensure the event is actually visible on screen (the even will span at the time slot where you
    // run the test, which may be before 8:00 or after 20:00)
    scrollUntilVisible(tag = "${CalendarScreenTestTags.EVENT_BLOCK}_Test Event")
    composeTestRule.onNodeWithTag("${CalendarScreenTestTags.EVENT_BLOCK}_Test Event").performClick()

    // We should now be on EventOverview (root test tag defined by the screen)
    composeTestRule.onNodeWithTag(EventOverviewScreenTestTags.SCREEN).assertIsDisplayed()

    // Click back and verify Calendar is shown again (by top bar title tag)
    composeTestRule
        .onNodeWithTag(EventOverviewScreenTestTags.BACK_BUTTON)
        .assertExists()
        .performClick()

    composeTestRule.onNodeWithTag(CalendarScreenTestTags.TOP_BAR_TITLE).assertExists()
  }
}
