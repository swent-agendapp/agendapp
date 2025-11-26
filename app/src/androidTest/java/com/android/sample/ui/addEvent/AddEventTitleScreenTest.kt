package com.android.sample.ui.addEvent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.calendar.addEvent.components.AddEventTitleAndDescriptionScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEventTitleScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { AddEventTitleAndDescriptionScreen() }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.COLOR_SELECTOR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AddEventTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

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

    composeTestRule
        .onNodeWithTag(AddEventTestTags.ERROR_MESSAGE, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun nextButtonDisabledWhenFieldsEmpty() {
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun nextButtonEnabledWhenTitleAndDescriptionValid() {
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performTextInput("Concert")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .performTextInput("Outdoor concert with friends")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsEnabled()
  }

  @Test
  fun selectingColorDoesNotEnableNextButtonWhenFieldsEmpty() {
    // Next button is disabled at start when title and description are empty
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsNotEnabled()

    // Open the color selector and pick the first color option
    composeTestRule.onNodeWithTag(AddEventTestTags.COLOR_SELECTOR).performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.COLOR_SELECTOR + "_option_0").performClick()

    // Changing only the color should not enable the Next button
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun selectingColorKeepsNextButtonEnabledWhenTitleAndDescriptionValid() {
    // Fill both fields with valid content
    composeTestRule.onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD).performTextInput("Concert")
    composeTestRule
        .onNodeWithTag(AddEventTestTags.DESCRIPTION_TEXT_FIELD)
        .performTextInput("Outdoor concert with friends")
    composeTestRule.waitForIdle()

    // Next button should be enabled once both fields are valid
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsEnabled()

    // Change the color using the ColorSelector
    composeTestRule.onNodeWithTag(AddEventTestTags.COLOR_SELECTOR).performClick()
    composeTestRule.onNodeWithTag(AddEventTestTags.COLOR_SELECTOR + "_option_0").performClick()

    // Next button should remain enabled after changing the color
    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertIsEnabled()
  }
}
