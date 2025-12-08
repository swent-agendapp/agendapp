package com.android.sample.ui.invitation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.invitation.createInvitation.InvitationCreationTestTags
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests written by AI

class InvitationOverviewScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTest {

  @get:Rule val composeTestRule = createComposeRule()

  override val organizationId = "test_org"

  @Before
  override fun setUp() {

    setSelectedOrganization()

    composeTestRule.setContent { InvitationOverviewScreen() }
  }
  // -------------------------------------------------------
  // ROOT + BASIC STRUCTURE
  // -------------------------------------------------------
  @Test
  fun root_isDisplayed() {
    composeTestRule.onNodeWithTag(InvitationOverviewScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.INVITATION_LIST)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // TOP BAR
  // -------------------------------------------------------
  @Test
  fun topBar_displaysCorrectTitle() {
    composeTestRule.onNodeWithTag(InvitationOverviewScreenTestTags.TITLE).assertIsDisplayed()
  }

  // -------------------------------------------------------
  // FAB BUTTON
  // -------------------------------------------------------
  @Test
  fun createInvitation_fab_isDisplayed() {
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // OPEN BOTTOM SHEET
  // -------------------------------------------------------
  @Test
  fun clickingCreateInvitation_opensBottomSheet() {
    // Bottom sheet should NOT exist at first
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET)
        .assertDoesNotExist()

    // Click floating action button
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON)
        .performClick()
    // Now bottom sheet is visible
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // DISMISS BOTTOM SHEET
  // -------------------------------------------------------
  @Test
  fun cancelInsideSheet_hidesBottomSheet() {
    // Open sheet
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON)
        .performClick()

    // Wait for sheet contents
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.CANCEL_BUTTON).assertIsDisplayed()

    // Click CANCEL
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.CANCEL_BUTTON).performClick()

    // After cancel → sheet disappears
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET)
        .assertDoesNotExist()
  }
}
