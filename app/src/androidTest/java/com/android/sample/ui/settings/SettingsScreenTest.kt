package com.android.sample.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.network.FakeConnectivityChecker
import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkTestBase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest : NetworkTestBase {

  override val fakeChecker = FakeConnectivityChecker(state = true)
  override val networkRepo = NetworkStatusRepository(fakeChecker)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    setupNetworkTestBase()
  }

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

  @Test
  fun noNetwork_disablesButtons() {
    simulateNoInternet()

    // Click flags
    var profileButtonClicked = false
    var adminButtonClicked = false
    var mapButtonClikded = false
    var orgButtonClicked = false
    var hourRecapClicked = false

    composeTestRule.setContent {
      SettingsScreen(
          settingsViewModel = FakeSettingsViewModel(networkStatusRepository = networkRepo),
          onNavigateToUserProfile = { profileButtonClicked = true },
          onNavigateToAdminInfo = { adminButtonClicked = true },
          onNavigateToMapSettings = { mapButtonClikded = true },
          onNavigateToOrganizationList = { orgButtonClicked = true },
          onNavigateToHourRecap = { hourRecapClicked = true })
    }

    // Click on profile button (should be enabled)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.PROFILE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(profileButtonClicked)

    // Click on admin button (should be enabled)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ADMIN_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(adminButtonClicked)

    // Click on map settings button (should be disabled)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.MAP_SETTINGS_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(!mapButtonClikded)

    // Assert snackbar is triggered
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SNACKBAR).assertIsDisplayed()

    // Assert snackbar message
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val expectedText = context.getString(R.string.network_error_message)
    composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()

    // Click on the snackbar to dismiss
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SNACKBAR).performClick()

    // Click on organization button (should be disabled)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(!orgButtonClicked)

    // Assert snackbar is triggered
    composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()

    // Click on the snackbar to dismiss
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SNACKBAR).performClick()

    // Click on hour recap button (should be disabled)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(!hourRecapClicked)

    // Assert snackbar is triggered
    composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()

    // Click on the snackbar to dismiss
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SNACKBAR).performClick()

    // Simulate internet restoration for other tests
    simulateInternetRestored()

    // Click on map settings button (should be enabled now)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.MAP_SETTINGS_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(mapButtonClikded)

    // Click on organization button (should be enabled now)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(orgButtonClicked)

    // Click on hour recap button (should be enabled now)
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(hourRecapClicked)
  }
}
