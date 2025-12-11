package com.android.sample.ui.editEvent

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.calendar.editEvent.EditEventFlow
import com.android.sample.ui.calendar.editEvent.EditEventStep
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.OrganizationTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI
/**
 * Basic UI component test for EditEventFlow.
 *
 * Verifies that the correct UI elements are displayed depending on the current EditEventStep.
 */
@RunWith(AndroidJUnit4::class)
class EditEventFlowUITest : FirebaseEmulatedTest() {

  @Before
  override fun setUp() {
    super.setUp()
    runBlocking {
      val helper = OrganizationTestHelper()
      helper.setupOrganizationWithUsers("org1")
    }
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun editEventFlow_displaysMainScreen_byDefault() {
    composeTestRule.setContent {
      EditEventFlow(eventId = "E123", editEventViewModel = EditEventViewModel())
    }

    composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventTestTags.DESCRIPTION_FIELD).assertIsDisplayed()
  }

  @Test
  fun editEventFlow_displaysAttendeesScreen_whenStepChanged() {
    val viewModel = EditEventViewModel()
    viewModel.setEditStep(EditEventStep.ATTENDEES)

    composeTestRule.setContent { EditEventFlow(eventId = "E123", editEventViewModel = viewModel) }

    composeTestRule.onNodeWithTag(EditEventTestTags.ATTENDANCE_WARNING_ACK_BUTTON).performClick()
    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventTestTags.PARTICIPANTS_LIST + "_Alice").assertExists()
  }
}
