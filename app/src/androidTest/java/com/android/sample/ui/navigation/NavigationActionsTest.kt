package com.android.sample.ui.navigation

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.android.sample.MainActivity
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.profile.AdminContactScreenTestTags
import com.android.sample.ui.profile.AdminInformation
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.replacement.ReplacementTestTags
import com.android.sample.ui.screens.HomeTestTags
import com.android.sample.ui.screens.HomeTestTags.CALENDAR_BUTTON
import com.android.sample.ui.settings.SettingsScreenTestTags
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [MainActivity] navigation. This test checks navigation flow between Home,
 * Calendar, Profile, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  // --- Navigation tests ---

  @Test
  fun navigate_to_all_add_forms() {
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
    // Go to replacement
    composeTestRule.onNodeWithTag(HomeTestTags.REPLACEMENT_BUTTON).assertExists().performClick()

    // Validate screen content
    composeTestRule.onNodeWithTag(ReplacementTestTags.SCREEN).assertIsDisplayed()
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

    // Back to Settings
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertExists().performClick()
  }

  // --- Intent tests ---

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun clickingEmail_opensEmailApp() {
    Intents.init()
    try {
      // Wait for home screen
      composeTestRule.waitUntilAtLeastOneExists(hasTestTag(HomeTestTags.SETTINGS_BUTTON))
      composeTestRule.onRoot().performTouchInput { click(Offset(1f, 1f)) }
      composeTestRule.waitForIdle()

      // Ensure window focus to prevent RootViewWithoutFocusException
      composeTestRule.runOnIdle {
        composeTestRule.activityRule.scenario.onActivity {
          it.window.decorView.requestFocus()
          it.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
      }
      Thread.sleep(300)

      // Navigate to Admin Contact screen
      composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).performClick()
      composeTestRule.onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON).performClick()
      composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).performClick()

      composeTestRule.waitForIdle()
      Thread.sleep(300)

      // Stub out email intent
      intending(hasAction(Intent.ACTION_SENDTO))
          .respondWith(Instrumentation.ActivityResult(0, null))

      // Perform click
      composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT).performClick()

      // Verify correct intent
      intended(
          allOf(
              hasAction(Intent.ACTION_SENDTO),
              hasData(Uri.parse("mailto:${AdminInformation.EMAIL}"))))
    } finally {
      Intents.release()
    }
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun clickingPhone_opensDialerApp() {
    Intents.init()
    try {
      composeTestRule.waitUntilAtLeastOneExists(hasTestTag(HomeTestTags.SETTINGS_BUTTON))
      composeTestRule.onRoot().performTouchInput { click(Offset(1f, 1f)) }
      composeTestRule.waitForIdle()

      // Ensure window focus to prevent RootViewWithoutFocusException
      composeTestRule.runOnIdle {
        composeTestRule.activityRule.scenario.onActivity {
          it.window.decorView.requestFocus()
          it.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
      }
      Thread.sleep(300)

      // Navigate to Admin Contact screen
      composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).performClick()
      composeTestRule.onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON).performClick()
      composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).performClick()

      composeTestRule.waitForIdle()
      Thread.sleep(300)

      // Stub out dialer intent
      intending(hasAction(Intent.ACTION_DIAL)).respondWith(Instrumentation.ActivityResult(0, null))

      // Perform click
      composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT).performClick()

      // Verify correct intent
      intended(
          allOf(
              hasAction(Intent.ACTION_DIAL),
              hasData(Uri.parse("tel:${AdminInformation.PHONE.replace(" ", "")}"))))
    } finally {
      Intents.release()
    }
  }
}
