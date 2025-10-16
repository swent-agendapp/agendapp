package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.AddEventAttendantScreen
import com.android.sample.ui.calendar.AddEventTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventAttendantScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventAttendantScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertIsDisplayed()
  }
}
