package com.android.sample.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.common.ButtonItem
import com.android.sample.ui.common.MainPageButton
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import com.android.sample.ui.replacement.ReplacementOverviewTestTags
import com.android.sample.ui.theme.*

object SettingsScreenTestTags {
  const val ROOT = "settings_screen"
  const val PROFILE_BUTTON = "profile_button"
  const val ADMIN_BUTTON = "admin_info_button"
  const val MAP_SETTINGS_BUTTON = "map_settings_button"
}

/** Settings screen with navigation to profile. */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreen(
    onNavigateToUserProfile: () -> Unit = {},
    onNavigateToAdminInfo: () -> Unit = {},
    onNavigateToMapSettings: () -> Unit = {},
    onNavigateToOrganizationList: () -> Unit = {},
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
) {
  val items =
      listOf(
          ButtonItem(
              stringResource(R.string.settings_profile_button),
              Icons.Default.Person,
              SettingsScreenTestTags.PROFILE_BUTTON,
              onClick = onNavigateToUserProfile),
          ButtonItem(
              stringResource(R.string.settings_admin_info_button),
              Icons.Default.AdminPanelSettings,
              SettingsScreenTestTags.ADMIN_BUTTON,
              onClick = onNavigateToAdminInfo),
          ButtonItem(
              stringResource(R.string.settings_map_settings_button),
              Icons.Default.Map,
              SettingsScreenTestTags.MAP_SETTINGS_BUTTON,
              onClick = onNavigateToMapSettings),
          ButtonItem(
              stringResource(R.string.settings_organization_selection_button),
              Icons.Default.Business,
              "app_info_button",
              onClick = {
                // Navigate to organization list
                onNavigateToOrganizationList()
              }))

  Scaffold(
      topBar = {
        MainPageTopBar(
            title = stringResource(R.string.settings_screen_title),
        )
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.padding(innerPadding).padding(PaddingMedium).fillMaxSize().semantics {
                  testTag = SettingsScreenTestTags.ROOT
                },
            horizontalAlignment = Alignment.CenterHorizontally) {
              Column(modifier = Modifier.testTag(ReplacementOverviewTestTags.CARD_LIST)) {
                items.forEach { item ->
                  MainPageButton(item, onClick = item.onClick)
                  Spacer(modifier = Modifier.height(SpacingMedium))
                }
              }
            }
      }
}
