package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.organization.data.Organization
import com.android.sample.ui.common.ButtonItem
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.Loading
import com.android.sample.ui.common.MainPageButton
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingMedium

object OrganizationListScreenTestTags {
  const val ROOT = "organizationListScreenRoot"
  const val ORGANIZATION_LIST = "organizationList"

  fun organizationItemTag(organizationName: String): String = "organizationItem_$organizationName"

  const val LOADING_INDICATOR = "loadingIndicator"

  const val ADD_ORGANIZATION_BUTTON = "addOrganizationButton"

  const val SNACK_BAR = "snackBar"

  const val PULL_TO_REFRESH = "pullToRefresh"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationListScreen(
    organizationViewModel: OrganizationViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    onOrganizationSelected: () -> Unit = {},
    onAddOrganizationClicked: () -> Unit = {},
) {
  val uiState by organizationViewModel.uiState.collectAsState()
  val snackBarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      snackBarHostState.showSnackbar(
          message = uiState.errorMsg.toString(), duration = SnackbarDuration.Short)
      organizationViewModel.clearErrorMsg()
    }
  }
  LaunchedEffect(Unit) { organizationViewModel.loadOrganizations() }


  Scaffold(
      topBar = {
        MainPageTopBar(
            title = stringResource(R.string.organization_list_title),
        )
      },
      floatingActionButton = {
        FloatingButton(
            onClick = onAddOrganizationClicked,
            icon = Icons.Default.Add,
            modifier = Modifier.testTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON))
      },
      modifier = Modifier.testTag(OrganizationListScreenTestTags.ROOT),
      snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.testTag(OrganizationListScreenTestTags.SNACK_BAR))
      }) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { organizationViewModel.refreshOrganizations() },
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .testTag(OrganizationListScreenTestTags.PULL_TO_REFRESH)) {
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .verticalScroll(rememberScrollState())
                          .padding(PaddingMedium)) {
                    if (uiState.isLoading) {
                      Loading(
                          label = stringResource(R.string.organization_loading),
                          modifier =
                              Modifier.testTag(OrganizationListScreenTestTags.LOADING_INDICATOR))
                    } else {
                      Spacer(modifier = Modifier.height(SpacingMedium))
                      OrganizationList(
                          organizations = uiState.organizations,
                          onOrganizationSelected = { organization ->
                            // Update selected organization in ViewModel
                            selectedOrganizationViewModel.selectOrganization(
                                orgId = organization.id)

                            // Invoke given callback after selection
                            onOrganizationSelected()
                          })
                    }
                  }
            }
      }
}

@Composable
fun OrganizationList(
    modifier: Modifier = Modifier,
    organizations: List<Organization> = emptyList(),
    onOrganizationSelected: (Organization) -> Unit = {},
) {
  Column(modifier = modifier.testTag(OrganizationListScreenTestTags.ORGANIZATION_LIST)) {
    organizations.forEach { organization ->
      val organizationItem =
          ButtonItem(
              title = organization.name,
              icon = Icons.Default.Business,
              tag = OrganizationListScreenTestTags.organizationItemTag(organization.name))
      MainPageButton(item = organizationItem, onClick = { onOrganizationSelected(organization) })
      Spacer(modifier = modifier.height(SpacingMedium))
    }
  }
}
