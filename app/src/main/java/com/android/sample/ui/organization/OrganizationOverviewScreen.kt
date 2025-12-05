package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingMedium
import kotlinx.coroutines.launch

object OrganizationOverviewScreenTestTags {
  const val ROOT = "organizationOverviewScreenRoot"
  const val ORGANIZATION_NAME_TEXT = "organizationNameText"
  const val MEMBER_COUNT_TEXT = "memberCountText"
  const val CHANGE_BUTTON = "changeButton"
  const val DELETE_BUTTON = "deleteButton"
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
  val errorMessage = uiState.errorMessageId

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
        Column(modifier = Modifier.padding(innerPadding).padding(PaddingMedium)) {

          // Later: Add other organization details as needed and management options

          // Display selected organization name
          Text(
              modifier =
                  Modifier.testTag(OrganizationOverviewScreenTestTags.ORGANIZATION_NAME_TEXT),
              text =
                  uiState.organizationName.ifEmpty {
                    stringResource(R.string.organization_none_selected)
                  })

          // Display member count
          Text(
              modifier = Modifier.testTag(OrganizationOverviewScreenTestTags.MEMBER_COUNT_TEXT),
              text = stringResource(R.string.organization_members) + ": ${uiState.memberCount}")

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
