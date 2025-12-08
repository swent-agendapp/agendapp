package com.android.sample.ui.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationOverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var fakeOrgRepo: FakeOrganizationRepository
  private lateinit var fakeAuthRepo: FakeAuthRepository
  private lateinit var fakeUserRepo: UsersRepositoryLocal
  private lateinit var vm: OrganizationOverviewViewModel
  private lateinit var selectedOrgVM: SelectedOrganizationViewModel

  private val fakeUser = User(id = "123", email = "test@test.com", displayName = "Tester")

  // Users for organization members
  private val user1 = User(id = "user1", email = "user1@test.com")
  private val user2 = User(id = "user2", email = "user2@test.com")
  private val user3 = User(id = "user3", email = "user3@test.com")

  @Before
  fun setup() {
    fakeOrgRepo = FakeOrganizationRepository()
    fakeAuthRepo = FakeAuthRepository(fakeUser)
    fakeUserRepo = UsersRepositoryLocal()
    vm = OrganizationOverviewViewModel(fakeOrgRepo,fakeUserRepo,fakeAuthRepo)
    selectedOrgVM = SelectedOrganizationVMProvider.viewModel
    selectedOrgVM.clearSelection()
  }

  @Test
  fun allElementsAreDisplayed() {
    composeTestRule.setContent {
      OrganizationOverViewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    composeTestRule.onNodeWithTag(OrganizationOverviewScreenTestTags.ROOT).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ORGANIZATION_NAME_TEXT)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.MEMBER_COUNT_TEXT)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.CHANGE_BUTTON)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.DELETE_BUTTON)
        .assertIsDisplayed()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun organizationDetailsAreDisplayed() = runTest {
    val org =
        Organization(
            id = "org1",
            name = "Test Organization")
    fakeOrgRepo.insertOrganization(org)
    selectedOrgVM.selectOrganization(org.id)

    composeTestRule.setContent {
      OrganizationOverViewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    // Organisation name
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ORGANIZATION_NAME_TEXT)
        .assertIsDisplayed()
        .assertTextEquals("Test Organization")

    // Member count
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.MEMBER_COUNT_TEXT)
        .assertIsDisplayed()
        .assertTextEquals("Members: 3")
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun noOrganizationSelectedDisplaysPlaceholderText() = runTest {
    selectedOrgVM.clearSelection()

    composeTestRule.setContent {
      OrganizationOverViewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ORGANIZATION_NAME_TEXT)
        .assertIsDisplayed()
        .assertTextEquals("No organization selected")
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun clickingChangeButtonClearsOrganization() = runTest {
    val org =
        Organization(id = "org1", name = "Org")
    fakeOrgRepo.insertOrganization(org)
    selectedOrgVM.selectOrganization(org.id)

    var changeClicked = false

    composeTestRule.setContent {
      OrganizationOverViewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onChangeOrganization = { changeClicked = true })
    }

    composeTestRule.onNodeWithTag(OrganizationOverviewScreenTestTags.CHANGE_BUTTON).performClick()

    assert(selectedOrgVM.selectedOrganizationId.value == null)
    assert(changeClicked)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun clickingDeleteButtonDeletesOrganizationAndCallsCallback() = runTest {
    val org =
        Organization(id = "org1", name = "To Delete")
    fakeOrgRepo.insertOrganization(org)
    selectedOrgVM.selectOrganization(org.id)

    var deleteCallbackCalled = false

    composeTestRule.setContent {
      OrganizationOverViewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onDeleteOrganization = { deleteCallbackCalled = true })
    }

    composeTestRule.onNodeWithTag(OrganizationOverviewScreenTestTags.DELETE_BUTTON).performClick()

    // Organization must be deleted
    assert(fakeOrgRepo.getOrganizationById("org1", fakeUser) == null)

    // Selection must be cleared
    assert(selectedOrgVM.selectedOrganizationId.value == null)

    // Callback must be called
    assert(deleteCallbackCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun snackBarAppearsOnError() = runTest {
    // Simulate an error in the ViewModel
    vm.setError(R.string.error_organization_not_found)

    composeTestRule.setContent {
      OrganizationOverViewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    // Verify that the snackbar with the error message is displayed
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ERROR_SNACKBAR)
        .assertIsDisplayed()
  }
}
