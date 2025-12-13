package com.android.sample.ui.editEvent

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.components.EditEventAttendantScreen
import com.android.sample.ui.calendar.editEvent.components.EditEventScreen
import com.android.sample.ui.theme.SampleAppTheme
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI
@RunWith(AndroidJUnit4::class)
class EditEventScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()

    setSelectedOrganization()
  }

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
    // Add Alice to participants to verify she appears in the list
    runBlocking {
      val user = User(id = "1", displayName = "Alice")
      UserRepositoryProvider.repository.newUser(user)
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    var saveClicked = false
    var backClicked = false

    composeTestRule.setContent {
      SampleAppTheme {
        EditEventAttendantScreen(onSave = { saveClicked = true }, onBack = { backClicked = true })
      }
    }

    // Click on the "Got it" button of the first pop-up
    composeTestRule.onNodeWithTag(EditEventTestTags.ATTENDANCE_WARNING_ACK_BUTTON).performClick()

    // Click a participant checkbox
    composeTestRule.onNodeWithText("Alice").performClick()

    // Test Save & Cancel
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(EditEventTestTags.BACK_BUTTON).performClick()

    assert(saveClicked)
    assert(backClicked)
  }

  @Test
  fun editEventAttendantScreen_toggleParticipantCheckbox() {
    // Add Alice to participants to verify she appears in the list
    runBlocking {
      val user = User(id = "1", displayName = "Alice")
      UserRepositoryProvider.repository.newUser(user)
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    composeTestRule.setContent { SampleAppTheme { EditEventAttendantScreen() } }

    val alice = composeTestRule.onNodeWithText("Alice")

    alice.assertExists()
    alice.performClick()
    alice.performClick()
  }

  @Test
  fun test_showStartAndEndTimePickers_areTriggeredOnClick() {
    // Arrange: Render the EditEventScreen (skip event loading for test simplicity)
    composeTestRule.setContent { EditEventScreen(eventId = "test_event_id", skipLoad = true) }

    // Verify the screen is properly loaded
    composeTestRule
        .onNodeWithTag(EditEventTestTags.TITLE_FIELD)
        .assertExists("EditEventScreen failed to load properly")

    // Act 1: Click the start time button (should trigger showStartTimePicker)
    composeTestRule
        .onNodeWithTag(EditEventTestTags.START_TIME_BUTTON)
        .assertExists("Start time button not found")
        .performClick()

    // Act 2: Click the end time button (should trigger showEndTimePicker)
    composeTestRule
        .onNodeWithTag(EditEventTestTags.END_TIME_BUTTON)
        .assertExists("End time button not found")
        .performClick()

    // Act 3: Click the save button (ensure it exists and doesn’t crash)
    composeTestRule
        .onNodeWithTag(EditEventTestTags.SAVE_BUTTON)
        .assertExists("Save button not found")
        .performClick()

    // Act 4: Click the cancel button (simulate user going back)
    composeTestRule
        .onNodeWithTag(EditEventTestTags.CANCEL_BUTTON)
        .assertExists("Cancel button not found")
        .performClick()

    // Assert: The screen should still exist and not crash
    composeTestRule
        .onNodeWithTag(EditEventTestTags.TITLE_FIELD)
        .assertExists("Screen should still be present after user interactions")
  }

  @Test
  fun editEventScreen_displaysColorSelector() {
    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E123",
            skipLoad = true, // we skip loading from repository to simplify the test
        )
      }
    }

    composeTestRule
        .onNodeWithTag(EditEventTestTags.CATEGORY_SELECTOR, useUnmergedTree = true)
        .performScrollTo()
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun editEventScreen_openColorSelector_showsColorOptions() {
    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E123",
            skipLoad = true, // we skip loading from repository to simplify the test
        )
      }
    }

    // Scroll to the ColorSelector and open the menu
    composeTestRule
        .onNodeWithTag(EditEventTestTags.CATEGORY_SELECTOR, useUnmergedTree = true)
        .performScrollTo()
        .performClick()

    // At least the first option should be visible when the menu is open
    composeTestRule
        .onNodeWithTag(EditEventTestTags.CATEGORY_SELECTOR + "_option_0", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun editEventScreen_selectingColor_doesNotEnableSaveWhenFieldsInvalid() {
    composeTestRule.setContent {
      SampleAppTheme {
        EditEventScreen(
            eventId = "E123",
            skipLoad = true, // we skip loading from repository to simplify the test
        )
      }
    }

    // At start, required fields are invalid, so the Save button must be disabled
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).assertExists().assertIsNotEnabled()

    // Open the ColorSelector and select the first color
    composeTestRule
        .onNodeWithTag(EditEventTestTags.CATEGORY_SELECTOR, useUnmergedTree = true)
        .performScrollTo()
        .performClick()

    composeTestRule
        .onNodeWithTag(EditEventTestTags.CATEGORY_SELECTOR + "_option_0", useUnmergedTree = true)
        .performClick()

    // Changing only the color should not make the form valid,
    // so the Save button must remain disabled
    composeTestRule.onNodeWithTag(EditEventTestTags.SAVE_BUTTON).assertIsNotEnabled()
  }
}
