package com.android.sample.ui.navigation

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.android.sample.AgendappNavigation
import com.android.sample.MainActivity
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.profile.AdminContactScreenTestTags
import com.android.sample.ui.profile.AdminInformation
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.screens.HomeTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [AgendappNavigation] navigation. This test checks the navigation flow
 * between Home, EditEvent, Calendar, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest {

  // Use AndroidComposeRule tied to your real activity (better focus handling in CI)
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  // Use IntentsRule to handle init/release automatically
  @get:Rule val intentsRule = IntentsRule()

  @Test
  fun navigate_to_all_add_forms() {
    // Go to Calendar
    composeTestRule.onNodeWithTag(HomeTestTags.ADD_EVENT_BUTTON).assertExists().performClick()
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
  fun navigate_to_profile_and_admin_profile_and_back() {
    // Go to Profile
    composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).assertExists()
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON)
        .assertExists()
        .performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
    // Go to Admin Contact
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON)
        .assertExists()
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE)
        .assertIsDisplayed()
    // Back to Profile
    composeTestRule
        .onNodeWithTag(AdminContactScreenTestTags.BACK_BUTTON)
        .assertExists()
        .performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
    // back to Settings
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertExists().performClick()
  }

  @Test
  fun clickingEmail_opensEmailApp() {
    // Stub out external email intent before starting
    intending(hasAction(Intent.ACTION_SENDTO)).respondWith(Instrumentation.ActivityResult(0, null))

    // Navigate through the app
    composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).performClick()

    // Wait for Compose to stabilize (important for CI)
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    // Perform the click on the email text
    composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT).performClick()

    // Verify correct intent was sent
    intended(
        allOf(
            hasAction(Intent.ACTION_SENDTO),
            hasData(Uri.parse("mailto:${AdminInformation.EMAIL}"))))
  }

  @Test
  fun clickingPhone_opensDialerApp() {
    // Stub out external dialer intent before starting
    intending(hasAction(Intent.ACTION_DIAL)).respondWith(Instrumentation.ActivityResult(0, null))

    // Navigate through the app
    composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).performClick()

    // Wait for Compose to stabilize (important for CI)
    composeTestRule.waitForIdle()
    Thread.sleep(500)

    // Perform the click on the phone text
    composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT).performClick()

    // Verify correct intent was sent
    intended(
        allOf(
            hasAction(Intent.ACTION_DIAL),
            hasData(Uri.parse("tel:${AdminInformation.PHONE.replace(" ", "")}"))))
  }
}
