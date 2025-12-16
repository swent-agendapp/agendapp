package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.android.sample.ui.invitation.useInvitation.UseInvitationBottomSheet
import com.android.sample.ui.invitation.useInvitation.UseInvitationViewModel
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingMedium
import kotlinx.coroutines.launch

object OrganizationListScreenTestTags {
  const val ROOT = "organizationListScreenRoot"
  const val ORGANIZATION_LIST = "organizationList"

  fun organizationItemTag(organizationName: String): String = "organizationItem_$organizationName"

  const val LOADING_INDICATOR = "loadingIndicator"

  const val ADD_ORGANIZATION_BUTTON = "addOrganizationButton"
  const val USE_INVITATION_BUTTON = "useInvitationButton"

  const val SNACK_BAR = "snackBar"

  const val PULL_TO_REFRESH = "pullToRefresh"
  const val USE_INVITATION_BOTTOM_SHEET = "useInvitationBottomSheet"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationListScreen(
    organizationViewModel: OrganizationViewModel = viewModel(),
    useInvitationViewModel: UseInvitationViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    onOrganizationSelected: () -> Unit = {},
    onAddOrganizationClicked: () -> Unit = {},
) {
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  val organizationUIState by organizationViewModel.uiState.collectAsState()
  val useInvitationUIState by useInvitationViewModel.uiState.collectAsState()
  val snackBarHostState = remember { SnackbarHostState() }

  // To trigger a Snackbar if the disabled button is clicked
  var disabledButtonClicked by remember { mutableStateOf(false) }

  val noNetworkErrorMsg = stringResource(R.string.network_error_message)
  // Observe the disabled button click and show Snackbar
  LaunchedEffect(disabledButtonClicked) {
    if (disabledButtonClicked) {
      snackBarHostState.showSnackbar(noNetworkErrorMsg)
      disabledButtonClicked = false
    }
  }

  val useInvitationErrMsg =
      useInvitationUIState.errorMessageId?.let {
        stringResource(id = useInvitationUIState.errorMessageId!!)
      }
  LaunchedEffect(useInvitationErrMsg) {
    useInvitationErrMsg?.let { errorMessage ->
      snackBarHostState.showSnackbar(errorMessage)
      useInvitationViewModel.setError(null)
    }
  }

  LaunchedEffect(organizationUIState.errorMsg, useInvitationErrMsg) {
    organizationUIState.errorMsg?.let {
      snackBarHostState.showSnackbar(
          message = organizationUIState.errorMsg.toString(), duration = SnackbarDuration.Short)
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
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(end = PaddingExtraSmall, bottom = PaddingExtraSmall)) {
              Column(
                  modifier = Modifier.align(Alignment.BottomEnd),
                  verticalArrangement = Arrangement.spacedBy(PaddingMedium)) {
                    FloatingButton(
                        onClick = {
                          if (organizationUIState.networkAvailable) {
                            scope.launch { sheetState.show() }
                            organizationViewModel.setShowUseInvitationBottomSheet(true)
                          } else {
                            disabledButtonClicked = true
                          }
                        },
                        icon = Icons.AutoMirrored.Filled.Login,
                        modifier =
                            Modifier.testTag(OrganizationListScreenTestTags.USE_INVITATION_BUTTON),
                        enabled = organizationUIState.networkAvailable)
                    FloatingButton(
                        onClick = {
                          if (organizationUIState.networkAvailable) {
                            onAddOrganizationClicked()
                          } else {
                            disabledButtonClicked = true
                          }
                        },
                        icon = Icons.Default.Add,
                        modifier =
                            Modifier.testTag(
                                OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON),
                        enabled = organizationUIState.networkAvailable,
                    )
                  }
            }
      },
      modifier = Modifier.testTag(OrganizationListScreenTestTags.ROOT),
      snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.testTag(OrganizationListScreenTestTags.SNACK_BAR))
      },
      content = { innerPadding ->
        PullToRefreshBox(
            isRefreshing = organizationUIState.isRefreshing,
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
                    if (organizationUIState.isLoading || useInvitationUIState.isTemptingToJoin) {
                      Loading(
                          label =
                              if (organizationUIState.isLoading)
                                  stringResource(R.string.organization_loading)
                              else stringResource(R.string.invitation_joining_organization),
                          modifier =
                              Modifier.fillMaxSize()
                                  .testTag(OrganizationListScreenTestTags.LOADING_INDICATOR))
                    } else {
                      Spacer(modifier = Modifier.height(SpacingMedium))
                      OrganizationList(
                          organizations = organizationUIState.organizations,
                          onOrganizationSelected = { organization ->
                            if (organizationUIState.networkAvailable) {
                              selectedOrganizationViewModel.selectOrganization(
                                  orgId = organization.id)
                              onOrganizationSelected()
                            } else {
                              disabledButtonClicked = true
                            }
                          },
                          enabled = organizationUIState.networkAvailable)
                    }
                  }
            }
      })
  if (organizationUIState.showUseInvitationBottomSheet) {
    ModalBottomSheet(
        modifier = Modifier.testTag(OrganizationListScreenTestTags.USE_INVITATION_BOTTOM_SHEET),
        onDismissRequest = { organizationViewModel.setShowUseInvitationBottomSheet(false) },
        sheetState = sheetState) {
          UseInvitationBottomSheet(
              useInvitationViewModel = useInvitationViewModel,
              onCancel = {
                scope.launch { sheetState.hide() }
                organizationViewModel.setShowUseInvitationBottomSheet(false)
              },
              onJoin = {
                scope.launch { sheetState.hide() }
                organizationViewModel.setShowUseInvitationBottomSheet(false)
              })
        }
  }
}

@Composable
fun OrganizationList(
    modifier: Modifier = Modifier,
    organizations: List<Organization> = emptyList(),
    onOrganizationSelected: (Organization) -> Unit = {},
    enabled: Boolean = true,
) {
  Column(modifier = modifier.testTag(OrganizationListScreenTestTags.ORGANIZATION_LIST)) {
    organizations.forEach { organization ->
      val organizationItem =
          ButtonItem(
              title = organization.name,
              icon = Icons.Default.Business,
              tag = OrganizationListScreenTestTags.organizationItemTag(organization.name))
      MainPageButton(
          item = organizationItem,
          onClick = { onOrganizationSelected(organization) },
          enabled = enabled)
      Spacer(modifier = modifier.height(SpacingMedium))
    }
  }
}
