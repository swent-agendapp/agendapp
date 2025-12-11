package com.android.sample.ui.invitation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.ui.invitation.createInvitation.CreateInvitationBottomSheet
import com.android.sample.ui.invitation.createInvitation.InvitationCreationTestTags
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests written by AI

class CreateInvitationBottomSheetTest :
    FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    setSelectedOrganization()
    composeTestRule.setContent { CreateInvitationBottomSheet(onCancel = {}) }
  }

  // -------------------------------------------------------
  // COUNT INPUT + BUTTONS
  // -------------------------------------------------------
  @Test
  fun countTextField_isDisplayed() {
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.COUNT_TEXT_FIELD).assertIsDisplayed()
  }

  @Test
  fun plusButton_isDisplayed() {
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.PLUS_BUTTON).assertIsDisplayed()
  }

  @Test
  fun minusButton_isDisplayed() {
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.MINUS_BUTTON).assertIsDisplayed()
  }

  // -------------------------------------------------------
  // NAVIGATION BUTTONS
  // -------------------------------------------------------
  @Test
  fun cancelButton_isDisplayed() {
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun createInvitationButton_isDisplayed() {
    composeTestRule
        .onNodeWithTag(InvitationCreationTestTags.CREATE_INVITATION_BUTTON)
        .assertIsDisplayed()
  }
}
