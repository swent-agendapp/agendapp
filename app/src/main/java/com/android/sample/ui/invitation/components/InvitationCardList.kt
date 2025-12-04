package com.android.sample.ui.invitation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingSmall

object InvitationCardListTestTags {
  const val EMPTY_INVITATION_LIST_MSG = "emptyInvitationListMessage"

  fun getTestTagForInvitationCard(invitation: Invitation): String =
      "invitationCard_${invitation.id}"
}

@Composable
fun InvitationCardList(
    invitations: List<Invitation>,
    modifier: Modifier = Modifier,
    onClickDelete: (Invitation) -> Unit = {}
) {
  if (invitations.isEmpty()) {
    Box(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center) {
          Text(
              stringResource(R.string.no_invitation_code_available),
              modifier =
                  modifier
                      .background(Color.White)
                      .testTag(InvitationCardListTestTags.EMPTY_INVITATION_LIST_MSG))
        }
  } else {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.spacedBy(SpacingSmall),
        contentPadding = PaddingValues(PaddingSmall),
    ) {
      items(invitations, key = { it.id }) { invitation ->
        InvitationCard(
            invitation = invitation,
            onClickDelete = { onClickDelete(invitation) },
            modifier =
                Modifier.testTag(
                    InvitationCardListTestTags.getTestTagForInvitationCard(invitation)))
      }
    }
  }
}
