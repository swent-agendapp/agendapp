package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventConfirmationBottomBar
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventConfirmationBottomBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventConfirmationBottomBar() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun finishButtonIsEnabled() {
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertIsEnabled()
  }
}
