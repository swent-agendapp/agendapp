package com.android.sample.ui.invitation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.sample.ui.invitation.useInvitation.UseInvitationBottomSheet
import com.android.sample.ui.invitation.useInvitation.UseInvitationTestTags
import com.android.sample.utils.FirebaseEmulatedTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UseInvitationBottomSheetTest : FirebaseEmulatedTest() {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    composeTestRule.setContent { UseInvitationBottomSheet(onCancel = {}, onJoin = {}) }
  }

  // -------------------------------------------------------
  // TEXT FIELD
  // -------------------------------------------------------
  @Test
  fun invitationCodeTextField_isDisplayed() {
    composeTestRule
        .onNodeWithTag(UseInvitationTestTags.INVITATION_CODE_TEXT_FIELD)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // NAVIGATION BUTTONS
  // -------------------------------------------------------
  @Test
  fun cancelButton_isDisplayed() {
    composeTestRule.onNodeWithTag(UseInvitationTestTags.CANCEL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun joinButton_isDisplayed() {
    composeTestRule.onNodeWithTag(UseInvitationTestTags.JOIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun joinButton_isDisabledInitially() {
    composeTestRule.onNodeWithTag(UseInvitationTestTags.JOIN_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun joinButton_isEnabledWhenCodeEntered() {
    composeTestRule
        .onNodeWithTag(UseInvitationTestTags.INVITATION_CODE_TEXT_FIELD)
        .performTextInput("ABC123")
    composeTestRule.onNodeWithTag(UseInvitationTestTags.JOIN_BUTTON).assertIsEnabled()
  }
}
