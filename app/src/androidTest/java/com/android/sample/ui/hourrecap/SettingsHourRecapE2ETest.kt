package com.android.sample.ui.hourRecap

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.Agendapp
import com.android.sample.ui.authentication.SignInScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.organization.AddOrganizationScreenTestTags
import com.android.sample.ui.organization.OrganizationListScreenTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsHourRecapE2ETest : FirebaseEmulatedTest() {

  companion object {
    private const val UI_AUTH_TIMEOUT = 10_000L
  }

  private val expectedName = "John Doe"
  private val expectedEmail = "john.doe@test.com"
  private val fakeGoogleIdToken =
      FakeJwtGenerator.createFakeGoogleIdToken(
          sub = "settings_test", name = expectedName, email = expectedEmail)
  private val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

  @get:Rule val compose = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    FirebaseEmulator.auth.signOut()
  }

  @Test
  fun navigateFromSettingsToHourRecap_success() {
    // Launch app
    compose.setContent { Agendapp(credentialManager = fakeCredentialManager) }
    compose.waitForIdle()

    // ---- Sign In ----
    compose.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed().performClick()

    compose.waitUntil(timeoutMillis = UI_AUTH_TIMEOUT) {
      compose
          .onAllNodesWithTag(OrganizationListScreenTestTags.ROOT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // ---- Create organization ----
    val orgName = "E2E_Test_Org"

    compose.onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON).performClick()

    compose
        .onNodeWithTag(AddOrganizationScreenTestTags.ORGANIZATION_NAME_TEXT_FIELD)
        .performTextInput(orgName)

    compose.onNodeWithTag(AddOrganizationScreenTestTags.CREATE_BUTTON).performClick()

    compose.waitForIdle()

    // Back to list → select organization
    compose
        .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(orgName))
        .performClick()

    compose.waitForIdle()

    // ---- Now we are in Calendar screen ----
    compose.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()

    // ---- Open Settings via bottom bar ----
    compose.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertIsDisplayed().performClick()

    compose.waitForIdle()

    // ---- Settings screen loaded ----
    compose.onNodeWithTag(SettingsScreenTestTags.ROOT).assertIsDisplayed()

    // ---- Click Hour Recap ----
    compose
        .onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON)
        .assertIsDisplayed()
        .performClick()

    compose.waitForIdle()

    // ---- Verify HourRecapScreen ----
    compose.onNodeWithTag(HourRecapTestTags.SCREEN_ROOT).assertIsDisplayed()
  }
}
