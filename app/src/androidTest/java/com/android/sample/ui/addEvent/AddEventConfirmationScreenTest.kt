package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventConfirmationScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventConfirmationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventConfirmationScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
  }
}
