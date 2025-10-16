package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.AddEventTimeAndRecurrenceScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTimeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventTimeAndRecurrenceScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.START_DATE_FIELD).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.START_TIME_BUTTON).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_TIME_BUTTON).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.RECURRENCE_STATUS_DROPDOWN).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).performScrollTo().assertIsDisplayed()
  }

  @Test
  fun nextButtonEnabledWhenFieldsAreValid() {
    composeTestRule.onNodeWithTag(AddEventTestTags.START_TIME_BUTTON).performScrollTo().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_TIME_BUTTON).performScrollTo().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.START_DATE_FIELD).performScrollTo().performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).performScrollTo().performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).performScrollTo().assertIsEnabled()
  }
}
