package com.android.sample.ui.invitation.createInvitation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.common.Loading
import com.android.sample.ui.invitation.InvitationOverviewViewModel
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingSmall

// Assisted by AI

private val TEXT_FIELD_WIDTH = 100.dp

/**
 * Contains UI test tags used throughout the Create Invitation bottom sheet.
 *
 * These tags are applied to interactive components such as buttons and text fields to allow UI
 * tests to reliably identify and interact with them.
 */
object InvitationCreationTestTags {
  const val COUNT_TEXT_FIELD = "countTextField"
  const val PLUS_BUTTON = "plusButton"
  const val MINUS_BUTTON = "minusButton"
  const val CANCEL_BUTTON = "cancelButton"
  const val INVITATION_ADDING_INDICATOR = "addingInvitationIndicator"
  const val CREATE_INVITATION_BUTTON = "createInvitationButton"
}

/**
 * A bottom-sheet UI component that allows the user to configure and create multiple invitation
 * codes.
 *
 * This composable displays:
 * - A counter for selecting how many invitations to create.
 * - Optional error messages provided by the [CreateInvitationViewModel].
 * - Navigation buttons to cancel or confirm creation.
 *
 * @param createInvitationViewModel The ViewModel providing state and creation logic. Defaults to
 *   the current `viewModel()` scoped to the composition.
 * @param onCancel Callback invoked when the user presses the cancel button.
 */
@Composable
fun CreateInvitationBottomSheet(
    createInvitationViewModel: CreateInvitationViewModel = viewModel(),
    invitationOverviewViewModel: InvitationOverviewViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    onCancel: () -> Unit = {},
    onCreate: () -> Unit = {}
) {

  val uiState by createInvitationViewModel.uiState.collectAsStateWithLifecycle()
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()

  Column(
      modifier = Modifier.fillMaxWidth().padding(PaddingLarge),
      horizontalAlignment = Alignment.CenterHorizontally) {

        // --- Count selector row ---
        if (uiState.isLoading) {
          Loading(
              modifier = Modifier.testTag(InvitationCreationTestTags.INVITATION_ADDING_INDICATOR),
              label = stringResource(R.string.invitations_creating))
        } else {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { createInvitationViewModel.decrement() },
                    modifier = Modifier.testTag(InvitationCreationTestTags.MINUS_BUTTON)) {
                      Icon(Icons.Default.Remove, contentDescription = null)
                    }

                OutlinedTextField(
                    value = uiState.count.toString(),
                    onValueChange = { createInvitationViewModel.setCount(it.toInt()) },
                    modifier =
                        Modifier.width(TEXT_FIELD_WIDTH)
                            .padding(horizontal = PaddingSmall)
                            .testTag(InvitationCreationTestTags.COUNT_TEXT_FIELD),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                IconButton(
                    onClick = { createInvitationViewModel.increment() },
                    modifier = Modifier.testTag(InvitationCreationTestTags.PLUS_BUTTON)) {
                      Icon(Icons.Default.Add, contentDescription = null)
                    }
              }
        }

        uiState.errorMsg?.let {
          Text(
              text = it,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(SpacingSmall))

        BottomNavigationButtons(
            onNext = {
              selectedOrgId?.let { orgId ->
                invitationOverviewViewModel.addInvitations(orgId, uiState.count)
                onCreate()
              }
            },
            onBack = onCancel,
            backButtonText = stringResource(R.string.cancel),
            nextButtonText = stringResource(R.string.create_invitation_button_text),
            canGoNext = createInvitationViewModel.canCreateInvitations(),
            backButtonTestTag = InvitationCreationTestTags.CANCEL_BUTTON,
            nextButtonTestTag = InvitationCreationTestTags.CREATE_INVITATION_BUTTON)
      }
}
