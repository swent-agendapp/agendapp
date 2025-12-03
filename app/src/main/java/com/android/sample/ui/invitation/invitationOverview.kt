package com.android.sample.ui.invitation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.invitation.components.InvitationCardList

const val DEFAULT_SWIPE_OFFSET = 0f

object InvitationOverviewScreenTestTags {
  const val ROOT = "invitationOverviewScreenRoot"
  const val EMPTY_INVITATION_LIST_MSG = "emptyInvitationListMessage"
  const val INVITATION_LIST = "invitationList"
  const val CREATE_INVITATION_BUTTON = "createInvitationButton"
  const val SWIPE_INVITATION_CARD_BUTTON = "SwipeInvitationCardButton"
  const val DELETE_INVITATION_BUTTON = "deleteInvitationButton"

  fun getTestTagForInvitationCard(invitation: Invitation): String =
      "invitationCard_${invitation.id}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationOverviewScreen(
    onBack: () -> Unit = {},
) {
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  val invitationList: List<Invitation> = listOf()
  var showSheet by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag(InvitationOverviewScreenTestTags.ROOT),
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.invitation_codes_title),
            canGoBack = true,
            onClick = onBack)
      },
      floatingActionButton = {
        FloatingActionButton(
            modifier = Modifier.testTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON),
            onClick = { showSheet = true }) {
              Button(onClick = { showSheet = true }) { Text("Create invitation") }
            }
      },
      content = { innerPadding ->
        InvitationCardList(
            invitations = invitationList,
            onClickDelete = {},
            modifier = Modifier.padding(innerPadding))
      })
  if (showSheet) {
    ModalBottomSheet(sheetState = sheetState, onDismissRequest = { showSheet = false }) {
      //            CreateInvitationBottomSheet(
      //                onCancel = {
      //                    scope.launch { sheetState.hide() }
      //                    showSheet = false
      //                }
      //            )
    }
  }
}
