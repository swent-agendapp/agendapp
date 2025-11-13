package com.android.sample.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.android.sample.ui.common.MainPageTopBar
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
) {
  Scaffold(
      topBar = {
        MainPageTopBar(
          title = stringResource(R.string.settings_screen_title),
        )
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.padding(innerPadding).fillMaxSize().semantics {
                  testTag = SettingsScreenTestTags.ROOT
                },
            horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(modifier = Modifier.height(SpacingLarge))

              SettingTab(
                  title = stringResource(R.string.settings_profile_button),
                  testTag = SettingsScreenTestTags.PROFILE_BUTTON,
                  onClick = onNavigateToUserProfile)

              SettingTab(
                  title = stringResource(R.string.settings_admin_info_button),
                  testTag = SettingsScreenTestTags.ADMIN_BUTTON,
                  onClick = onNavigateToAdminInfo)

              SettingTab(
                  title = stringResource(R.string.settings_map_settings_button),
                  testTag = SettingsScreenTestTags.MAP_SETTINGS_BUTTON,
                  onClick = onNavigateToMapSettings)
            }
      }
}

@Composable
fun SettingTab(
    title: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit = {}
) {
  Surface(
      modifier =
          modifier
              .padding(horizontal = PaddingMedium)
              .fillMaxWidth()
              .testTag(testTag)
              .border(
                  width = BorderWidthThin,
                  color = MaterialTheme.colorScheme.outline,
                  shape = MaterialTheme.shapes.medium)
              .clickable(onClick = onClick),
      shape = MaterialTheme.shapes.medium,
      color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.padding(horizontal = SpacingLarge, vertical = SpacingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(text = title, style = MaterialTheme.typography.bodyLarge)

              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
      }

  Spacer(modifier = Modifier.height(SpacingLarge))
}
