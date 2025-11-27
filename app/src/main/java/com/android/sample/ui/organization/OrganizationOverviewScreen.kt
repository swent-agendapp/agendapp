package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingMedium
import kotlinx.coroutines.launch

@Composable
fun OrganizationOverViewScreen(
    onNavigateBack: () -> Unit = {},
    onChangeOrganization: () -> Unit = {},
    onDeleteOrganization: () -> Unit = {},
    organizationOverviewViewModel: OrganizationOverviewViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
) {

  val coroutineScope = rememberCoroutineScope()
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()

  // Load organization details when selectedOrgId changes
  LaunchedEffect(Unit) {
    selectedOrgId?.let { organizationOverviewViewModel.fillSelectedOrganizationDetails(it) }
  }

  val uiState by organizationOverviewViewModel.uiState.collectAsState()

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.settings_organization_selection_button),
            onClick = onNavigateBack,
            backButtonTestTags = "")
      }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(PaddingMedium)) {

          // Later: Add other organization details as needed and management options

          // Display selected organization name
          Text(
              text =
                  uiState.organizationName.ifEmpty {
                    stringResource(R.string.organization_none_selected)
                  })

          // Display member count
          Text(text = stringResource(R.string.organization_members) + ": ${uiState.memberCount}")

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
              nextButtonTestTag = "",
              backButtonTestTag = "")
        }
      }
}
