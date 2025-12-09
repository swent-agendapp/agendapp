package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.ElevationExtraLow
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingSmall
import kotlinx.coroutines.launch

object OrganizationOverviewScreenTestTags {
  const val ROOT = "organizationOverviewScreenRoot"
  const val ORGANIZATION_NAME_TEXT = "organizationNameText"
  const val MEMBER_COUNT_TEXT = "memberCountText"
  const val CHANGE_BUTTON = "changeButton"
  const val DELETE_BUTTON = "deleteButton"
  const val INVITATION_BUTTON = "invitationButton"
  const val ERROR_SNACKBAR = "errorSnackBar"
}

/**
 * Composable function for the Organization Overview screen.
 *
 * @param onNavigateBack Callback for navigating back.
 * @param onChangeOrganization Callback for changing the organization.
 * @param onDeleteOrganization Callback for deleting the organization.
 * @param organizationOverviewViewModel ViewModel for managing organization overview state.
 * @param selectedOrganizationViewModel ViewModel for managing selected organization state.
 */
@Composable
fun OrganizationOverViewScreen(
    onNavigateBack: () -> Unit = {},
    onChangeOrganization: () -> Unit = {},
    onDeleteOrganization: () -> Unit = {},
    onInvitationClick: () -> Unit = {},
    organizationOverviewViewModel: OrganizationOverviewViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
) {

  // Remember coroutine scope and snackbar host state for handling async operations and messages
  val coroutineScope = rememberCoroutineScope()
  val snackBarHostState = remember { SnackbarHostState() }

  // Collect state from ViewModels
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()
  val uiState by organizationOverviewViewModel.uiState.collectAsState()

  // Get error message string if available
  val errorMessage = uiState.errorMessageId?.let { id -> stringResource(id) }

  // Load organization details when selectedOrgId changes
  LaunchedEffect(Unit) {
    selectedOrgId?.let { organizationOverviewViewModel.fillSelectedOrganizationDetails(it) }
  }

  // Show error messages in a snack-bar
  LaunchedEffect(errorMessage) {
    errorMessage?.let { msg ->
      snackBarHostState.showSnackbar(msg)
      organizationOverviewViewModel.clearError()
    }
  }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.settings_organization_selection_button),
            onClick = onNavigateBack,
            backButtonTestTags = "")
      },
      snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.testTag(OrganizationOverviewScreenTestTags.ERROR_SNACKBAR))
      },
      modifier = Modifier.testTag(OrganizationOverviewScreenTestTags.ROOT)) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(PaddingMedium),
            verticalArrangement = Arrangement.SpaceBetween) {
              Column {
                Spacer(modifier = Modifier.height(SpacingSmall))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadiusLarge),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = ElevationExtraLow)) {
                      Column(
                          modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
                          verticalArrangement = Arrangement.spacedBy(SpacingSmall)) {
                            Text(
                                modifier =
                                    Modifier.testTag(
                                        OrganizationOverviewScreenTestTags.ORGANIZATION_NAME_TEXT),
                                text =
                                    uiState.organizationName.ifEmpty {
                                      stringResource(R.string.organization_none_selected)
                                    },
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Text(
                                modifier =
                                    Modifier.testTag(
                                        OrganizationOverviewScreenTestTags.MEMBER_COUNT_TEXT),
                                text =
                                    stringResource(R.string.organization_members) +
                                        ": ${uiState.memberCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                          }
                    }
              }

          // Display member count
          Text(
              modifier = Modifier.testTag(OrganizationOverviewScreenTestTags.MEMBER_COUNT_TEXT),
              text = stringResource(R.string.organization_members) + ": ${uiState.memberCount}")

          // Here is an hardcoded string, but this button is only here temporarily, so we do not
          // need to write "Invitations" in strings.xml
          PrimaryButton(
              text = "Invitations",
              onClick = onInvitationClick,
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(OrganizationOverviewScreenTestTags.INVITATION_BUTTON))

          // Bottom buttons (Change / Delete)
          BottomNavigationButtons(
              onNext = {
                coroutineScope.launch {
                  organizationOverviewViewModel.deleteSelectedOrganization(selectedOrgId)
                  onDeleteOrganization()
                }
              },
              onBack = {
                organizationOverviewViewModel.clearSelectedOrganization()
                onChangeOrganization()
              },
              canGoNext = true,
              canGoBack = true,
              nextButtonText = stringResource(R.string.delete),
              backButtonText = stringResource(R.string.change),
              nextButtonTestTag = OrganizationOverviewScreenTestTags.DELETE_BUTTON,
              backButtonTestTag = OrganizationOverviewScreenTestTags.CHANGE_BUTTON)
        }
      }
}
