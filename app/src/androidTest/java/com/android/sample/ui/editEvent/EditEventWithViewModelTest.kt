package com.android.sample.ui.editEvent

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.calendar.EventRepositoryLocal
import com.android.sample.ui.calendar.EditEventAttendantScreen
import com.android.sample.ui.calendar.EditEventScreen
import com.android.sample.ui.calendar.EditEventTestTags
import com.android.sample.ui.calendar.EditEventViewModel
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ViewModel-driven integration tests for EditEventScreen & EditEventAttendantScreen These tests
 * verify UI + ViewModel interaction consistency.
 */
@RunWith(AndroidJUnit4::class)
class EditEventWithViewModelTest {

  @get:Rule val composeTestRule = createComposeRule()

  // -------------------------------------------------------------------------
  // 1. Test: EditEventScreen disables Save button when fields are empty
  // -------------------------------------------------------------------------
  @Test
  fun editEventScreen_showsErrorWhenFieldsEmpty_withViewModel() {
    val fakeRepository = EventRepositoryLocal(preloadSampleData = true)
    val fakeViewModel = EditEventViewModel(fakeRepository)

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E001",
            editEventViewModel = fakeViewModel,
            onSave = {},
            onCancel = {},
            onEditParticipants = {},
            skipLoad = true)
      }
    }

    // clear title & description
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).performTextClearance()
    // wait for UI idle
    composeTestRule.waitForIdle()
    // Save button should be disabled
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }

  // -------------------------------------------------------------------------
  // 2. Test: EditEventScreen - Save & Cancel callbacks trigger correctly
  // -------------------------------------------------------------------------
  @Test
  fun editEventScreen_saveAndCancelTriggersCallbacks_withViewModel() {
    var saveClicked = false
    var cancelClicked = false

    val fakeRepository = EventRepositoryLocal(preloadSampleData = true)
    val fakeViewModel = EditEventViewModel(fakeRepository)

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E001",
            editEventViewModel = fakeViewModel,
            onSave = { saveClicked = true },
            onCancel = { cancelClicked = true },
            onEditParticipants = {})
      }
    }

    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).performTextInput("Meeting")

    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD)
        .performTextInput("Plan update")

    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.CANCEL_BUTTON).performClick()

    // verify callbacks triggered
    assert(saveClicked)
    assert(cancelClicked)

    // verify ViewModel state updated
    val uiState = fakeViewModel.uiState.value
    assert(uiState.title == "Meeting")
    assert(uiState.description == "Plan update")
  }

  // -------------------------------------------------------------------------
  // 3. Test: EditEventAttendantScreen - Selecting participants works
  // -------------------------------------------------------------------------
  @Test
  fun editEventAttendantScreen_selectsParticipantsCorrectly_withViewModel() {
    var saveClicked = false
    var backClicked = false

    val fakeRepository = EventRepositoryLocal(preloadSampleData = true)
    val fakeViewModel = EditEventViewModel(fakeRepository)

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventAttendantScreen(
            editEventViewModel = fakeViewModel,
            onSave = { saveClicked = true },
            onBack = { backClicked = true })
      }
    }

    composeTestRule.onNodeWithText("Select participants").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice").performClick()

    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.BACK_BUTTON).performClick()

    assert(saveClicked)
    assert(backClicked)

    // verify ViewModel updated with selected participant
    val uiState = fakeViewModel.uiState.value
    assert(uiState.participants.contains("Alice"))
  }

  // -------------------------------------------------------------------------
  // 4. Test: EditEventAttendantScreen - Toggle same participant twice
  // -------------------------------------------------------------------------
  @Test
  fun editEventAttendantScreen_toggleParticipantCheckbox_withViewModel() {
    val fakeRepository = EventRepositoryLocal(preloadSampleData = true)
    val fakeViewModel = EditEventViewModel(fakeRepository)

    composeTestRule.setContent {
      SampleAppTheme { EditEventAttendantScreen(editEventViewModel = fakeViewModel) }
    }

    val alice = composeTestRule.onNodeWithText("Alice")

    alice.assertExists()
    alice.performClick() // select
    alice.performClick() // unselect

    // verify ViewModel updated to remove participant
    val uiState = fakeViewModel.uiState.value
    assert(!uiState.participants.contains("Alice"))
  }
}
