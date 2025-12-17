package com.android.sample.ui.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.network.FakeConnectivityChecker
import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkTestBase
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.FakeInvitationRepository
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.ui.invitation.useInvitation.UseInvitationViewModel
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationListScreenTest : NetworkTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  override val fakeChecker = FakeConnectivityChecker(state = true)
  override val networkRepo = NetworkStatusRepository(fakeChecker)

  private lateinit var fakeOrganizationViewModel: FakeOrganizationViewModel
  private lateinit var fakeUseInvitationViewModel: UseInvitationViewModel
  private lateinit var selectedOrgVM: SelectedOrganizationViewModel

  private val organizations = listOf(Organization(name = "Org 1"), Organization(name = "Org 2"))
  private val userId = "1"

  @Before
  fun setUp() {
    setupNetworkTestBase()

    fakeOrganizationViewModel = FakeOrganizationViewModel(networkStatusRepository = networkRepo)
    fakeUseInvitationViewModel =
        UseInvitationViewModel(
            invitationRepository = FakeInvitationRepository(),
            authRepository = FakeAuthRepository(),
            organizationRepository = FakeOrganizationRepository())
    selectedOrgVM = SelectedOrganizationVMProvider.viewModel
  }

  @After
  fun tearDown() {
    // Clear selected organization after each test
    selectedOrgVM.clearSelection()
  }

  @Test
  fun loadingIndicatorIsDisplayed() {

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Mock loading state
    composeTestRule.runOnIdle { fakeOrganizationViewModel.setLoading() }

    // Assert loading indicator is shown
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun organizationsAreDisplayed() {
    // Mock organizations loaded state
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Assert organizations are displayed
    organizations.forEach { org ->
      composeTestRule
          .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org.name))
          .assertIsDisplayed()
    }
  }

  @Test
  fun selectingOrganizationUpdatesSelectedState() {
    // Mock organizations loaded state
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)

    composeTestRule.setContent {
      OrganizationListScreen(
          organizationViewModel = fakeOrganizationViewModel,
          selectedOrganizationViewModel = selectedOrgVM)
    }

    val org2 = organizations[1]

    // Perform click on the second organization
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org2.name))
        .performClick()

    // Assert Org 2 is selected
    assert(selectedOrgVM.selectedOrganizationId.value == org2.id)
  }

  @Test
  fun errorMessageIsShown() {
    // Mock error state
    val errorMessage = "Fake error occurred"
    fakeOrganizationViewModel.setError(errorMessage)

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Check that the Snack bar is displayed
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.SNACK_BAR).assertExists()

    // Check that the error message is shown
    composeTestRule.onNodeWithText(errorMessage).assertExists().assertIsDisplayed()
  }

  @Test
  fun pullToRefreshIsDisplayedWhenRefreshing() {
    // Mock refreshing state
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)
    fakeOrganizationViewModel.setRefreshing(true)

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Assert pull-to-refresh component exists
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.PULL_TO_REFRESH).assertExists()
  }

  @Test
  fun organizationsAreUpdatedAfterRefresh() {
    // Start with initial organizations
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Verify initial organizations are displayed
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(organizations[0].name))
        .assertIsDisplayed()

    // Simulate refresh with new data
    val newOrganizations =
        listOf(Organization(name = "New Org 1"), Organization(name = "New Org 2"))
    fakeOrganizationViewModel.setOrganizations(organizations = newOrganizations, userId = userId)

    // Verify new organizations are displayed
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag("New Org 1"))
        .assertIsDisplayed()
  }

  @Test
  fun useInvitationErrorMessageIsShown() {
    fakeUseInvitationViewModel.setError(R.string.error_joining_organization)

    composeTestRule.setContent {
      OrganizationListScreen(
          useInvitationViewModel = fakeUseInvitationViewModel,
          organizationViewModel = fakeOrganizationViewModel)
    }

    // Check that the Snack bar is displayed
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.SNACK_BAR).assertExists()
  }

  @Test
  fun useInvitationButtonIsDisplayed() {
    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Check that the Use Invitation button is displayed
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.USE_INVITATION_BUTTON)
        .assertExists()
  }

  @Test
  fun loadingIndicatorDuringInvitationJoin() {
    // Mock joining state
    fakeUseInvitationViewModel.setIsTemptingToJoin(true)

    composeTestRule.setContent {
      OrganizationListScreen(
          useInvitationViewModel = fakeUseInvitationViewModel,
          organizationViewModel = fakeOrganizationViewModel)
    }

    // Assert loading indicator is shown during invitation join
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun noNetworkButtonsDisabled() {
    // Simulate no network
    simulateNoInternet()

    // Mock organizations loaded state
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)

    composeTestRule.setContent {
      OrganizationListScreen(
          organizationViewModel = fakeOrganizationViewModel,
          onAddOrganizationClicked = { fail("Add button should not be triggered") })
    }

    // Try clicking on each organization
    organizations.forEach { org ->
      composeTestRule
          .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org.name))
          .performClick()
    }

    // Assert that selected organization has NOT changed
    assert(selectedOrgVM.selectedOrganizationId.value == null)

    // Try clicking on Use Invitation button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.USE_INVITATION_BUTTON)
        .performClick()

    // Try clicking on Add Organization button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .performClick()
  }

  @Test
  fun noNetworkSnackbarIsDisplayed() {
    // Simulate no network
    simulateNoInternet()

    // Mock organizations loaded state
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Perform click on the second organization
    val org2 = organizations[1]
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org2.name))
        .performClick()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val expectedText = context.getString(R.string.network_error_message)

    // Assert that the Snack bar is displayed
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.SNACK_BAR).assertIsDisplayed()

    // Assert that the snackbar has the expected text displayed
    composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()

    // Click on the snackbar to dismiss it
    composeTestRule.onNodeWithText(expectedText).performClick()

    // Click on the add organization button to trigger snackbar again
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .performClick()

    // Assert that the Snack bar is displayed
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.SNACK_BAR).assertIsDisplayed()

    // Assert that the snackbar has the expected text displayed
    composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()
  }

  @Test
  fun noNetworkThenRecoverButtonsEnabled() {
    // Simulate no network
    simulateNoInternet()

    // Mock organizations loaded state
    fakeOrganizationViewModel.setOrganizations(organizations = organizations, userId = userId)

    composeTestRule.setContent {
      OrganizationListScreen(
          organizationViewModel = fakeOrganizationViewModel,
          selectedOrganizationViewModel = selectedOrgVM,
          onAddOrganizationClicked = {})
    }

    // Assert buttons do nothing when clicked
    organizations.forEach { org ->
      composeTestRule
          .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org.name))
          .performClick()
    }

    // Assert that selected organization has NOT changed
    assert(selectedOrgVM.selectedOrganizationId.value == null)

    // Try clicking on Use Invitation button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.USE_INVITATION_BUTTON)
        .performClick()

    // Try clicking on Add Organization button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .performClick()

    // Assert that the Snackbar is displayed for network error
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val expectedText = context.getString(R.string.network_error_message)
    composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()

    // Simulate network recovery
    simulateInternetRestored()

    // Try clicking on each organization
    organizations.forEach { org ->
      composeTestRule
          .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org.name))
          .performClick()
    }

    // Assert that selected organization has changed
    assert(selectedOrgVM.selectedOrganizationId.value == organizations.last().id)

    // Try clicking on Use Invitation button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.USE_INVITATION_BUTTON)
        .performClick()

    // Try clicking on Add Organization button
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.ADD_ORGANIZATION_BUTTON)
        .performClick()
  }
}
