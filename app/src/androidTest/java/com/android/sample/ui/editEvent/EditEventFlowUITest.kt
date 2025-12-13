package com.android.sample.ui.editEvent

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.ui.calendar.editEvent.EditEventFlow
import com.android.sample.ui.calendar.editEvent.EditEventStep
import com.android.sample.ui.calendar.editEvent.EditEventTestTags
import com.android.sample.ui.calendar.editEvent.EditEventViewModel
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
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
class EditEventFlowUITest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()
    setSelectedOrganization()

    // Use local user repository for tests
    UserRepositoryProvider.repository = UsersRepositoryLocal()
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
    // Add Alice to participants to verify she appears in the list
    runBlocking {
      val user = User(id = "1", displayName = "Alice")
      UserRepositoryProvider.repository.newUser(user)
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
    }

    val viewModel = EditEventViewModel()
    viewModel.setEditStep(EditEventStep.ATTENDEES)

    composeTestRule.setContent { EditEventFlow(eventId = "E123", editEventViewModel = viewModel) }

    composeTestRule.onNodeWithTag(EditEventTestTags.ATTENDANCE_WARNING_ACK_BUTTON).performClick()
    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditEventTestTags.PARTICIPANTS_LIST + "_Alice").assertExists()

    // Clean up
    runBlocking { UserRepositoryProvider.repository.deleteUser("1") }
  }
}
