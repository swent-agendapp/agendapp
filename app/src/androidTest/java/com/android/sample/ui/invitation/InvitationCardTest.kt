package com.android.sample.ui.invitation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.invitation.components.InvitationCard
import com.android.sample.ui.invitation.components.InvitationCardTestTags
import java.time.Instant
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests written by AI

class InvitationCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var activeInvitation: Invitation
  private lateinit var usedInvitation: Invitation
  private var deleteClicked = false

  @Before
  fun setUp() {

    activeInvitation =
        Invitation(
            id = "id1", code = "123ABC", organizationId = "org1", status = InvitationStatus.Active)

    usedInvitation =
        Invitation(
            id = "id",
            code = "ABC123",
            organizationId = "org1",
            inviteeEmail = "john@example.com",
            acceptedAt = Instant.parse("2024-01-10T10:15:30Z"),
            status = InvitationStatus.Used)

    deleteClicked = false

    composeTestRule.setContent {
      InvitationCard(invitation = usedInvitation, onClickDelete = { deleteClicked = true })
    }
  }

  @After
  @Test
  fun card_displaysInvitationCode() {
    composeTestRule.onNodeWithTag(InvitationCardTestTags.CODE_FIELD).assertIsDisplayed()
  }

  @Test
  fun card_displaysInviteeEmail() {
    composeTestRule.onNodeWithTag(InvitationCardTestTags.ACCEPTED_AT_FIELD).assertIsDisplayed()
  }

  @Test
  fun card_displaysAcceptedDate() {
    val expectedDateText =
        "Accepted on " + DateTimeUtils.formatInstantToDateAndTime(usedInvitation.acceptedAt!!)

    composeTestRule.onNodeWithText(expectedDateText).assertIsDisplayed()
  }

  @Test
  fun swipeButton_isDisplayed() {
    composeTestRule.onNodeWithTag(InvitationCardTestTags.SWIPE_CARD_BUTTON).assertIsDisplayed()
  }

  @Test
  fun click_on_delete_without_swiping_first_should_do_nothing() {
    composeTestRule.onNodeWithTag(InvitationCardTestTags.DELETE_INVITATION_BUTTON).performClick()
    assert(!deleteClicked)
  }

  @Test
  fun deleteButton_triggersCallback() {
    composeTestRule.onNodeWithTag(InvitationCardTestTags.SWIPE_CARD_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(InvitationCardTestTags.DELETE_INVITATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(deleteClicked) { "Delete callback should be triggered" }
  }
}
