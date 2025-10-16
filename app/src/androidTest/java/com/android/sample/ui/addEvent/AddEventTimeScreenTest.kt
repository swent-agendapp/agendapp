package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
    composeTestRule.onNodeWithTag(AddEventTestTags.START_DATE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.START_TIME_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_TIME_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.RECURRENCE_STATUS_DROPDOWN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun nextButtonEnabledWhenFieldsAreValid() {
    composeTestRule.onNodeWithTag(AddEventTestTags.START_TIME_BUTTON).performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_TIME_BUTTON).performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.START_DATE_FIELD).performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsEnabled()
  }
}
