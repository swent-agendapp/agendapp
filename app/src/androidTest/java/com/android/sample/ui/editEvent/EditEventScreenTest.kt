package com.android.sample.ui.editEvent

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.ui.calendar.editEvent.EditEventAttendantScreen
import com.android.sample.ui.calendar.editEvent.EditEventScreen
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditEventScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Test to verify that all UI elements are displayed on the Edit Event screen
  @Test
  fun editEventScreen_displaysAllUIElements() {
    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(eventId = "E123", onSave = {}, onCancel = {}, onEditParticipants = {})
      }
    }

    composeTestRule.onRoot(useUnmergedTree = true).printToLog("EditEventScreenTree")

    composeTestRule
        .onNodeWithTag(EditEventTestTags.TITLE_FIELD, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.RECURRENCE_DROPDOWN, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.START_DATE_FIELD, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.END_DATE_FIELD, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.START_TIME_BUTTON, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.END_TIME_BUTTON, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()

    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodes(hasTestTag(EditEventTestTags.SAVE_BUTTON), useUnmergedTree = true)
        .onFirst()
        .assertExists()

    composeTestRule
        .onAllNodes(hasTestTag(EditEventTestTags.CANCEL_BUTTON), useUnmergedTree = true)
        .onFirst()
        .assertExists()
  }

  @Test
  fun editEventAttendantScreen_selectsParticipantsCorrectly() {
    var saveClicked = false
    var backClicked = false

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventAttendantScreen(onSave = { saveClicked = true }, onBack = { backClicked = true })
      }
    }

    // Verify participant list rendered
    composeTestRule.onNodeWithText("Select participants").assertIsDisplayed()

    // Click a participant checkbox
    composeTestRule.onNodeWithText("Alice").performClick()

    // Test Save & Cancel
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.BACK_BUTTON).performClick()

    assert(saveClicked)
    assert(backClicked)
  }

  @Test
  fun editEventScreen_showsErrorWhenFieldsEmpty() {
    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(eventId = "E001", onSave = {}, onCancel = {}, onEditParticipants = {})
      }
    }

    // delete title and description
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).performTextClearance()

    // Save button should be disabled
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun editEventScreen_saveAndCancelTriggersCallbacks() {
    var saveClicked = false
    var cancelClicked = false

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E004",
            onSave = { saveClicked = true },
            onCancel = { cancelClicked = true },
            onEditParticipants = {})
      }
    }

    // 填入合法 title / description 确保 Save 可点击
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextInput("Meeting")
    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD)
        .performTextInput("Plan update")

    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.CANCEL_BUTTON).performClick()

    assert(saveClicked)
    assert(cancelClicked)
  }

  @Test
  fun editEventAttendantScreen_toggleParticipantCheckbox() {
    composeTestRule.setContent { SampleAppTheme { EditEventAttendantScreen() } }

    val alice = composeTestRule.onNodeWithText("Alice")

    alice.assertExists()
    alice.performClick()
    alice.performClick()
  }
}
