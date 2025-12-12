package com.android.sample.ui.invitation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.ui.invitation.components.InvitationCardList
import com.android.sample.ui.invitation.components.InvitationCardListTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests written by AI

class InvitationCardListTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var invitationA: Invitation
  private lateinit var invitationB: Invitation
  private var deletedInvitation: Invitation? = null

  @Before
  fun setUp() {

    invitationA =
        Invitation(
            id = "idA", code = "AAA111", organizationId = "org1", status = InvitationStatus.Active)

    invitationB =
        Invitation(
            id = "idB", code = "BBB222", organizationId = "org1", status = InvitationStatus.Used)

    deletedInvitation = null
  }

  // ---------------------------------------------------
  // EMPTY LIST TEST
  // ---------------------------------------------------
  @Test
  fun emptyInvitationList_showsEmptyMessage() {
    composeTestRule.setContent { InvitationCardList(invitations = emptyList()) }

    composeTestRule
        .onNodeWithTag(InvitationCardListTestTags.EMPTY_INVITATION_LIST_MSG)
        .assertIsDisplayed()
  }

  // ---------------------------------------------------
  // NON-EMPTY LIST TEST
  // ---------------------------------------------------
  @Test
  fun invitationList_displaysAllInvitationCards() {
    val list = listOf(invitationA, invitationB)

    composeTestRule.setContent { InvitationCardList(invitations = list) }

    // each card must be displayed
    composeTestRule
        .onNodeWithTag(InvitationCardListTestTags.getTestTagForInvitationCard(invitationA))
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(InvitationCardListTestTags.getTestTagForInvitationCard(invitationB))
        .assertIsDisplayed()

    // empty message must NOT be shown
    composeTestRule
        .onNodeWithTag(InvitationCardListTestTags.EMPTY_INVITATION_LIST_MSG)
        .assertDoesNotExist()
  }
}
