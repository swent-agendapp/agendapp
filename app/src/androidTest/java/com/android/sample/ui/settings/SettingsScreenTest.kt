package com.android.sample.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun settingsScreen_displaysAndBackButtonWorks() {
    composeTestRule.setContent { SettingsScreen() }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).isDisplayed()
  }

  @Test
  fun settingsScreen_profileButtonWorks() {
    var profileClicked = false
    composeTestRule.setContent {
      SettingsScreen(onNavigateToUserProfile = { profileClicked = true })
    }

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(profileClicked)
  }

  @Test
  fun settingsScreen_adminButtonWorks() {
    var adminClicked = false
    composeTestRule.setContent { SettingsScreen(onNavigateToAdminInfo = { adminClicked = true }) }

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ADMIN_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(adminClicked)
  }

  @Test
  fun settingsScreen_mapSettingsButtonWorks() {
    var mapSettingsClicked = false
    composeTestRule.setContent {
      SettingsScreen(onNavigateToMapSettings = { mapSettingsClicked = true })
    }

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.MAP_SETTINGS_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(mapSettingsClicked)
  }

  @Test
  fun settingsScreen_organizationButtonWorks() {
    var organizationClicked = false
    composeTestRule.setContent {
      SettingsScreen(onNavigateToOrganizationList = { organizationClicked = true })
    }

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    assert(organizationClicked)
  }
}
