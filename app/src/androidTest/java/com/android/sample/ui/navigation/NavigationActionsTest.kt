package com.android.sample.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.android.sample.Agendapp_Navigation
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [Agendapp_Navigation] navigation. This test checks the navigation flow
 * between Home, EditEvent, Calendar, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun navigate_to_all_add_forms() {
    composeTestRule.setContent { Agendapp_Navigation() }

    // Go to Calendar
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertExists().performClick()
    // Validate screen content
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertExists().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.FINISH_BUTTON).assertExists().performClick()

    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertIsDisplayed()
  }
}
