package com.android.sample.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.replacement.ReplacementOverviewTestTags
import com.android.sample.ui.theme.*
import com.android.sample.ui.theme.Palette

object SettingsScreenTestTags {
  const val ROOT = "settings_screen"
  const val PROFILE_BUTTON = "profile_button"
  const val ADMIN_BUTTON = "admin_info_button"
  const val MAP_SETTINGS_BUTTON = "map_settings_button"
  const val ORGANIZATION_BUTTON = "organization_selection_button"
  const val HOURRECAP_BUTTON = "hour_recap_button"
  const val SNACKBAR = "snackbar"
}

/** Settings screen with navigation to profile. */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToUserProfile: () -> Unit = {},
    onNavigateToAdminInfo: () -> Unit = {},
    onNavigateToMapSettings: () -> Unit = {},
    onNavigateToOrganizationList: () -> Unit = {},
    onNavigateToHourRecap: () -> Unit = {}
) {

  val uiState by settingsViewModel.uiState.collectAsState()

  // Host state for displaying a snack bar in case of errors
  val snackbarHostState = remember { SnackbarHostState() }

  // To trigger a Snackbar if the disabled button is clicked
  var disabledButtonClicked by remember { mutableStateOf(false) }

  val noNetworkErrorMsg = stringResource(R.string.network_error_message)
  // Observe the disabled button click and show Snackbar
  LaunchedEffect(disabledButtonClicked) {
    if (disabledButtonClicked) {
      snackbarHostState.showSnackbar(noNetworkErrorMsg)
      disabledButtonClicked = false
    }
  }

  Scaffold(
      snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.testTag(SettingsScreenTestTags.SNACKBAR))
      },
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
              Card(
                  modifier = Modifier.fillMaxWidth().testTag(ReplacementOverviewTestTags.CARD_LIST),
                  shape = RoundedCornerShape(CornerRadiusExtraLarge),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(vertical = SpacingSmall)) {
                      SettingsItemRow(
                          title = stringResource(R.string.settings_profile_button),
                          icon = Icons.Default.Person,
                          testTag = SettingsScreenTestTags.PROFILE_BUTTON,
                          onClick = onNavigateToUserProfile)

                      DividerSpacer()

                      SettingsItemRow(
                          title = stringResource(R.string.settings_admin_info_button),
                          icon = Icons.Default.AdminPanelSettings,
                          testTag = SettingsScreenTestTags.ADMIN_BUTTON,
                          onClick = onNavigateToAdminInfo)

                      DividerSpacer()

                      SettingsItemRow(
                          title = stringResource(R.string.settings_map_settings_button),
                          icon = Icons.Default.Map,
                          testTag = SettingsScreenTestTags.MAP_SETTINGS_BUTTON,
                          onClick = {
                            if (uiState.networkAvailable) {
                              onNavigateToMapSettings()
                            } else {
                              disabledButtonClicked = true
                            }
                          },
                          enabled = uiState.networkAvailable)

                      DividerSpacer()

                      SettingsItemRow(
                          title = stringResource(R.string.settings_organization_selection_button),
                          icon = Icons.Default.Business,
                          testTag = SettingsScreenTestTags.ORGANIZATION_BUTTON,
                          onClick = {
                            if (uiState.networkAvailable) {
                              onNavigateToOrganizationList()
                            } else {
                              disabledButtonClicked = true
                            }
                          },
                          enabled = uiState.networkAvailable)

                      DividerSpacer()

                      SettingsItemRow(
                          title = stringResource(R.string.hour_recap),
                          icon = Icons.Default.AccessTime,
                          testTag = SettingsScreenTestTags.HOURRECAP_BUTTON,
                          onClick = {
                            if (uiState.networkAvailable) {
                              onNavigateToHourRecap()
                            } else {
                              disabledButtonClicked = true
                            }
                          },
                          enabled = uiState.networkAvailable)

                      DividerSpacer()
                    }
                  }
            }
      }
}

@Composable
private fun SettingsItemRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .testTag(testTag)
              .clickable(onClick = onClick)
              .then(if (!enabled) Modifier.alpha(AlphaMedium) else Modifier)
              .padding(horizontal = PaddingMedium, vertical = SpacingMedium),
      verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(heightMedium)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center) {
              Icon(imageVector = icon, contentDescription = null, tint = CircusPalette.Primary)
            }

        Spacer(modifier = Modifier.width(SpacingMedium))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(WeightExtraHeavy))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) Palette.Gray else Palette.Gray.copy(alpha = AlphaMedium))
      }
}

@Composable
private fun DividerSpacer() {
  Spacer(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = PaddingMedium)
              .height(heightExtraSmall)
              .background(Palette.LightGray.copy(alpha = AlphaLow)))
}
