package com.android.sample.ui.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.FakeInvitationRepository
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.ui.invitation.useInvitation.UseInvitationViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var fakeOrganizationViewModel: FakeOrganizationViewModel
  private lateinit var fakeUseInvitationViewModel: UseInvitationViewModel
  private lateinit var selectedOrgVM: SelectedOrganizationViewModel

  private val organizations = listOf(Organization(name = "Org 1"), Organization(name = "Org 2"))

  @Before
  fun setUp() {
    fakeOrganizationViewModel = FakeOrganizationViewModel()
    fakeUseInvitationViewModel =
        UseInvitationViewModel(
            invitationRepository = FakeInvitationRepository(),
            authRepository = FakeAuthRepository(),
            organizationRepository = FakeOrganizationRepository())
    selectedOrgVM = SelectedOrganizationVMProvider.viewModel
  }

  @Test
  fun loadingIndicatorIsDisplayed() {
    // Mock loading state
    fakeOrganizationViewModel.setLoading()

    composeTestRule.setContent {
      OrganizationListScreen(organizationViewModel = fakeOrganizationViewModel)
    }

    // Assert loading indicator is shown
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun organizationsAreDisplayed() {
    // Mock organizations loaded state
    fakeOrganizationViewModel.setOrganizations(organizations)

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
    fakeOrganizationViewModel.setOrganizations(organizations)

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
    fakeOrganizationViewModel.setOrganizations(organizations)
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
    fakeOrganizationViewModel.setOrganizations(organizations)

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
    fakeOrganizationViewModel.setOrganizations(newOrganizations)

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
}
