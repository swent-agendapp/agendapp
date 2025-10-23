package com.android.sample.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.android.sample.AgendappNavigation
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.profile.AdminContactScreenTestTags
import com.android.sample.ui.profile.ProfileScreenTestTags
import com.android.sample.ui.screens.HomeTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
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

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigate_to_all_add_forms() {
        composeTestRule.setContent { AgendappNavigation() }

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
    fun navigate_to_profile_and_admin_profile() {
        composeTestRule.setContent { AgendappNavigation() }
        // Go to Profile
        composeTestRule.onNodeWithTag(HomeTestTags.SETTINGS_BUTTON).assertExists().performClick()
        composeTestRule.onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON).assertExists().performClick()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
        // Go to Admin Contact
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).assertExists().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE).assertIsDisplayed()
    }
}