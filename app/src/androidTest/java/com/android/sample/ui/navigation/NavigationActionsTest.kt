package com.android.sample.ui.navigation

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.android.sample.Agendapp
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.profile.AdminContactScreenTestTags
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.replacement.ReplacementTestTags
import com.android.sample.ui.screens.HomeTestTags
import com.android.sample.ui.screens.HomeTestTags.CALENDAR_BUTTON
import com.android.sample.ui.settings.LanguageSelectionSectionTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [Agendapp] navigation. This test checks the navigation flow between Home,
 * EditEvent, Calendar, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val composeTestRule = createComposeRule()

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
  fun navigate_to_profile_and_admin_profile_and_back() {
    composeTestRule.setContent { Agendapp() }
    // Go to Profile
    composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).assertExists()
    composeTestRule
        .onNodeWithTag(LanguageSelectionSectionTestTags.ROOT)
        .assertExists()
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
  fun navigate_to_map_and_back() {
    composeTestRule.setContent { Agendapp() }

    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertExists().performClick()

    composeTestRule.onNodeWithTag(MapScreenTestTags.GOOGLE_MAP_SCREEN).assertExists()

    composeTestRule
        .onNodeWithTag(MapScreenTestTags.MAP_GO_BACK_BUTTON)
        .assertExists()
        .performClick()

    composeTestRule.onNodeWithTag(HomeTestTags.MAP_BUTTON).assertExists()
  }
}
