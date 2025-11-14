package com.android.sample.e2eTests

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.Agendapp
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.profile.AdminContactScreenTestTags
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import com.github.se.bootcamp.ui.authentication.SignInScreenTestTags
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class AddEventE2ETest : FirebaseEmulatedTest() {

  // Timeout for UI authentication operations
  // This is used to wait for the UI to update after authentication actions
  companion object {
    private const val UI_AUTH_TIMEOUT = 10_000L
  }

  val eventTitle = "Event Test"
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
  fun signIn_createWeeklyEvent() {

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

    // Wait for sign-in to complete
    composeTestRule.waitUntil(timeoutMillis = UI_AUTH_TIMEOUT) {
      // Verify Calendar screen exists
      composeTestRule
          .onAllNodesWithTag(CalendarScreenTestTags.ROOT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Check that a user is signed in after sign in
    assert(FirebaseEmulator.auth.currentUser != null)

    // Verify Calendar screen is displayed
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()

    // Go to Event Creation Assistant
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.ADD_EVENT_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // Fill First Form
    composeTestRule
      .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
      .performTextInput(eventTitle)

    composeTestRule
      .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
      .performTextInput(eventDescription)

    composeTestRule
      .onNodeWithTag(AddEventTestTags.NEXT_BUTTON)
      .performClick()

    composeTestRule
      .onNodeWithTag(AddEventTestTags.RECURRENCE_STATUS_DROPDOWN)
      .performClick()

    composeTestRule
      .onNodeWithTag(AddEventTestTags.recurrenceTag(RecurrenceStatus.Weekly))
      .performClick()

    composeTestRule
      .onNodeWithTag(AddEventTestTags.START_TIME_BUTTON)
      .performClick()

    onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
      .perform(PickerActions.setTime(14, 45))
    onView(withText("OK")).perform(click())

    composeTestRule
      .onNodeWithTag(AddEventTestTags.END_TIME_BUTTON)
      .performClick()

    onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
      .perform(PickerActions.setTime(15, 45))
    onView(withText("OK")).perform(click())

    composeTestRule
      .onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD)
      .performClick()

    composeTestRule
      .onNodeWithTag(AddEventTestTags.NEXT_BUTTON)
      .performClick()

    composeTestRule
      .onAllNodesWithTag(AddEventTestTags.CHECK_BOX_EMPLOYEE)[0]
      .performClick()

    composeTestRule
      .onNodeWithTag(AddEventTestTags.CREATE_BUTTON)
      .performClick()

    composeTestRule
      .onNodeWithTag(AddEventTestTags.FINISH_BUTTON)
      .performClick()

    composeTestRule
      .onNodeWithTag(CalendarScreenTestTags.EVENT_BLOCK + "_" + eventTitle)
      .assertIsDisplayed()
  }
}
