package com.android.sample.ui.navigation

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.android.sample.Agendapp
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.authentication.SignInScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.CalendarScreenTestTags.ADD_EVENT_BUTTON
import com.android.sample.ui.calendar.addEvent.AddEventTestTags
import com.android.sample.ui.category.EditCategoryScreenTestTags
import com.android.sample.ui.common.BottomBarTestTags
import com.android.sample.ui.hourRecap.HourRecapTestTags
import com.android.sample.ui.invitation.InvitationOverviewScreenTestTags
import com.android.sample.ui.organization.AddOrganizationScreenTestTags
import com.android.sample.ui.organization.OrganizationListScreenTestTags
import com.android.sample.ui.organization.OrganizationOverviewScreenTestTags
import com.android.sample.ui.settings.SettingsScreenTestTags
import com.android.sample.utils.FakeCredentialManager
import com.android.sample.utils.FakeJwtGenerator
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for [Agendapp] navigation. This test checks the navigation flow between Home,
 * EditEvent, Calendar, and Settings screens.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AgendappNavigationTest : FirebaseEmulatedTest() {

  // Create a fake Google ID token for testing
  val fakeGoogleIdToken =
      FakeJwtGenerator.createFakeGoogleIdToken("login_test", "12345", email = "test@example.com")

  // Create a FakeCredentialManager with the fake token
  val fakeCredentialManager = FakeCredentialManager.create(fakeGoogleIdToken)

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  override fun setUp() {
    super.setUp()
    // Ensure a user is signed in before each test (use runBlocking to call suspend function)
    runBlocking {
      FirebaseEmulator.signInWithFakeGoogleUser(fakeGoogleIdToken)
      UserRepositoryProvider.repository.newUser(
          AuthRepositoryProvider.repository.getCurrentUser()!!)
    }
  }

  @Test
  fun ensure_sign_in_screen_if_not_signed_in() = runTest {
    // Sign out user before launching app
    FirebaseEmulator.auth.signOut()

    // Launch app
    composeTestRule.setContent { Agendapp(credentialManager = fakeCredentialManager) }

    // Ensure Sign-In screen is displayed
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_MESSAGE).assertIsDisplayed()

    // Perform Sign-In
    composeTestRule.onNodeWithTag(SignInScreenTestTags.LOGIN_BUTTON).assertIsDisplayed()
  }

  @Test
  fun ensure_organization_list_if_signed_in() = runTest {

    // Launch app with user already signed in
    composeTestRule.setContent { Agendapp() }

    // Verify Organization list screen is displayed (user remains signed in)
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.ROOT).assertIsDisplayed()
  }

  @Test
  fun navigate_to_replacement() {
    composeTestRule.setContent { Agendapp() }

    createOrganizationAndNavigateToCalendar()

    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_REPLACEMENT).assertExists().performClick()
  }

  @Test
  fun addEventAndResetsTheFieldsTheNextTime() {
    composeTestRule.setContent { Agendapp() }

    createOrganizationAndNavigateToCalendar()

    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertExists().performClick()

    // Step 1: Title + description
    composeTestRule
        .onNodeWithTag(AddEventTestTags.TITLE_TEXT_FIELD)
        .assertExists()
        .performTextInput("Test Event")

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()

    // Step 2: Time + recurrence
    // OneTime => end recurrence exists but disabled
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertExists()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsNotEnabled()

    // Select Weekly recurrence
    composeTestRule
        .onNodeWithTag(AddEventTestTags.recurrenceTag(RecurrenceStatus.Weekly))
        .assertExists()
        .performClick()

    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertExists()
    composeTestRule.onNodeWithTag(AddEventTestTags.END_RECURRENCE_FIELD).assertIsEnabled()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()

    // Step 3: Participants => MUST select at least one
    composeTestRule
        .onNodeWithText("12345", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(AddEventTestTags.NEXT_BUTTON).assertExists().performClick()

    // Confirmation / create
    composeTestRule.onNodeWithTag(AddEventTestTags.CREATE_BUTTON).assertExists().performClick()

    // Back to calendar
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ADD_EVENT_BUTTON).performClick()
  }

  @Test
  fun goBottomBarIcons() {
    composeTestRule.setContent { Agendapp() }

    createOrganizationAndNavigateToCalendar()

    composeTestRule.onNodeWithTag(BottomBarTestTags.BOTTOM_BAR).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_CALENDAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_REPLACEMENT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_REPLACEMENT).assertExists().performClick()

    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).performClick()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_CALENDAR).performClick()
    composeTestRule.onNodeWithTag(CalendarScreenTestTags.ROOT).assertIsDisplayed()
  }

  @Test
  fun goToInvitationCodesScreen() {
    composeTestRule.setContent { Agendapp() }

    createOrganizationAndNavigateToCalendar()

    composeTestRule.onNodeWithTag(BottomBarTestTags.BOTTOM_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertExists().performClick()

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(OrganizationOverviewScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.INVITATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(InvitationOverviewScreenTestTags.ROOT).assertIsDisplayed()
  }

  @Test
  fun goToEditCategoryScreen() {
    composeTestRule.setContent { Agendapp() }

    createOrganizationAndNavigateToCalendar()

    composeTestRule.onNodeWithTag(BottomBarTestTags.BOTTOM_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertExists().performClick()

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(OrganizationOverviewScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.EDIT_CATEGORY_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
  }

  @Test
  fun navigate_fromSettings_toHourRecap() {
    composeTestRule.setContent { Agendapp() }

    // Step 1: create org → navigate to calendar
    createOrganizationAndNavigateToCalendar()

    // Step 2: open settings
    composeTestRule.onNodeWithTag(BottomBarTestTags.ITEM_SETTINGS).assertExists().performClick()

    // Step 3: assert Settings screen displayed
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ROOT).assertIsDisplayed()

    // Step 4: click “Hour Recap”
    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.HOURRECAP_BUTTON)
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    // Step 5: verify Hour Recap screen is shown
    composeTestRule.onNodeWithTag(HourRecapTestTags.SCREEN_ROOT).assertExists().assertIsDisplayed()
  }

  // Helper function to create an organization and navigate to its calendar
  private fun createOrganizationAndNavigateToCalendar() {
    val organizationName = "Test Organization"
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    // Fill Organization Name
    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.ORGANIZATION_NAME_TEXT_FIELD)
        .assertIsDisplayed()
        .performTextInput(organizationName)

    // Click on Create button
    composeTestRule
        .onNodeWithTag(AddOrganizationScreenTestTags.CREATE_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }
}
