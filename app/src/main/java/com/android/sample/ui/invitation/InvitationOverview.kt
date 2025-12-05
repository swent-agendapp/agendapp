package com.android.sample.ui.invitation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.invitation.components.InvitationCardList
import com.android.sample.ui.invitation.createInvitation.CreateInvitationBottomSheet
import kotlinx.coroutines.launch

// Assisted by AI

const val DEFAULT_SWIPE_OFFSET = 0f

/**
 * Contains the test tags used across the invitation overview screen.
 *
 * These tags allow UI tests to reliably locate and interact with top-level layout elements, the
 * invitation list, the FAB that opens the creation sheet, and the bottom sheet itself.
 */
object InvitationOverviewScreenTestTags {
  const val ROOT = "invitationOverviewScreenRoot"
  const val TITLE = "title"
  const val INVITATION_LIST = "invitationList"
  const val CREATE_INVITATION_BUTTON = "createInvitationButton"
  const val CREATE_INVITATION_BOTTOM_SHEET = "createInvitationBottomSheet"
}

/**
 * Displays the overview screen for invitation codes of a given organization.
 *
 * This screen shows:
 * - A top bar with a title and a back button.
 * - A list of existing invitation codes (currently provided as an empty list).
 * - A floating action button that opens a modal bottom sheet to create new invitations.
 *
 * The creation UI is presented using a [ModalBottomSheet].
 *
 * @param invitationOverviewViewModel The ViewModel holding the UI state of the screen
 * @param organizationId The ID of the organization whose invitation codes are being managed.
 * @param onBack Callback invoked when the user presses the back button in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationOverviewScreen(
    invitationOverviewViewModel: InvitationOverviewViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    onBack: () -> Unit = {},
) {
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()
  selectedOrgId?.let { orgId ->
    LaunchedEffect(orgId) { invitationOverviewViewModel.loadInvitations(orgId) }
  }
  val uiState by invitationOverviewViewModel.uiState.collectAsStateWithLifecycle()

  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  Scaffold(
      modifier = Modifier.testTag(InvitationOverviewScreenTestTags.ROOT),
      topBar = {
        SecondaryPageTopBar(
            modifier = Modifier.testTag(InvitationOverviewScreenTestTags.TITLE),
            title = stringResource(R.string.invitation_codes_title),
            canGoBack = true,
            onClick = onBack)
      },
      floatingActionButton = {
        FloatingButton(
            modifier = Modifier.testTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON),
            onClick = {
              scope.launch { sheetState.hide() }
              invitationOverviewViewModel.showBottomSheet()
            })
      },
      content = { innerPadding ->
        Box(modifier = Modifier.testTag(InvitationOverviewScreenTestTags.INVITATION_LIST)) {
          InvitationCardList(
              invitations = uiState.invitations,
              onClickDelete = { invitation ->
                invitationOverviewViewModel.deleteInvitation(invitationId = invitation.id)
              },
              modifier = Modifier.padding(innerPadding))
        }
      })
  if (uiState.showBottomSheet) {
    ModalBottomSheet(
        modifier =
            Modifier.testTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET),
        sheetState = sheetState,
        onDismissRequest = { invitationOverviewViewModel.dismissBottomSheet() }) {
          CreateInvitationBottomSheet(
              onCancel = {
                scope.launch { sheetState.hide() }
                invitationOverviewViewModel.dismissBottomSheet()
              },
              onCreate = {
                selectedOrgId?.let { orgId ->
                  scope.launch {
                    sheetState.hide()
                    invitationOverviewViewModel.dismissBottomSheet()
                    invitationOverviewViewModel.loadInvitations(orgId)
                  }
                }
              },
              scope = scope)
        }
  }
}

@Preview
@Composable
fun InvitationOverviewScreenPreview() {
  InvitationOverviewScreen()
}
