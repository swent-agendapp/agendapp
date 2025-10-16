package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.sample.ui.calendar.AddEventTestTags
import com.android.sample.ui.calendar.AddEventTitleAndDescriptionScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventTitleAndDescriptionScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

  /*

  @Test
  fun canEnterTitle() {
    val text = "My Event"
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performTextInput(text)
    composeTestRule
        .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
        .assertTextContains(text, substring = true)
  }

  @Test
  fun canEnterDescription() {
    val text = "A very fun event!"
    composeTestRule.onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD).performTextInput(text)
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .assertTextContains(text, substring = true)
  }

  @Test
  fun emptyTitleShowsErrorMessage() {
    val invalidTitle = " "
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performTextInput(invalidTitle)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD).performClick()

    composeTestRule
        .onNodeWithTag(AddEventTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun emptyDescriptionShowsErrorMessage() {
    val invalidDescription = " "
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .performTextInput(invalidDescription)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performClick()

    composeTestRule
        .onNodeWithTag(AddEventTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun nextButtonDisabledWhenFieldsEmpty() {
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsNotEnabled()
  }

   */

  @Test
  fun nextButtonEnabledWhenTitleAndDescriptionValid() {
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performTextInput("Concert")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .performTextInput("Outdoor concert with friends")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsEnabled()
  }
}
