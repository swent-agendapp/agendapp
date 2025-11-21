package com.android.sample.ui.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.organization.Organization
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var fakeViewModel: FakeOrganizationViewModel
  private lateinit var selectedOrgViewModel: SelectedOrganizationViewModel

  private val organizations =
      listOf(
          Organization(name = "Org 1", admins = emptyList(), members = emptyList()),
          Organization(name = "Org 2", admins = emptyList(), members = emptyList()))

  @Before
  fun setUp() {
    fakeViewModel = FakeOrganizationViewModel()
    selectedOrgViewModel = SelectedOrganizationViewModel()
  }

  @Test
  fun loadingIndicatorIsDisplayed() {
    // Mock loading state
    fakeViewModel.setLoading()

    composeTestRule.setContent { OrganizationListScreen(organizationViewModel = fakeViewModel) }

    // Assert loading indicator is shown
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun organizationsAreDisplayed() {
    // Mock organizations loaded state
    fakeViewModel.setOrganizations(organizations)

    composeTestRule.setContent { OrganizationListScreen(organizationViewModel = fakeViewModel) }

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
    fakeViewModel.setOrganizations(organizations)

    composeTestRule.setContent {
      OrganizationListScreen(
          organizationViewModel = fakeViewModel,
          selectedOrganizationViewModel = selectedOrgViewModel)
    }

    val org2 = organizations[1]

    // Perform click on the second organization
    composeTestRule
        .onNodeWithTag(OrganizationListScreenTestTags.organizationItemTag(org2.name))
        .performClick()

    // Assert Org 2 is selected
    assert(selectedOrgViewModel.selectedOrganizationId.value == org2.id)
  }

  @Test
  fun errorMessageIsShown() {
    // Mock error state
    val errorMessage = "Fake error occurred"
    fakeViewModel.setError(errorMessage)

    composeTestRule.setContent { OrganizationListScreen(organizationViewModel = fakeViewModel) }

    // Check that the Snack bar is displayed
    composeTestRule.onNodeWithTag(OrganizationListScreenTestTags.SNACK_BAR).assertExists()

    // Check that the error message is shown
    composeTestRule.onNodeWithText(errorMessage).assertExists().assertIsDisplayed()
  }
}
