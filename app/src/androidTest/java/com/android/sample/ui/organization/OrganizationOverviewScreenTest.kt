package com.android.sample.ui.organization

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.ui.common.MemberListTestTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
  private val user1 = User(id = "user1", email = "user1@test.com", displayName = "User 1")
  private val user2 = User(id = "user2", email = "user2@test.com", displayName = "User 2")
  private val user3 = User(id = "user3", email = "user3@test.com", displayName = "User 3")

  @Before
  fun setup() = runBlocking {
    fakeOrgRepo = FakeOrganizationRepository()
    fakeAuthRepo = FakeAuthRepository(fakeUser)
    fakeUserRepo = UsersRepositoryLocal()
    vm = OrganizationOverviewViewModel(fakeOrgRepo, fakeUserRepo, fakeAuthRepo)
    selectedOrgVM = SelectedOrganizationVMProvider.viewModel
    selectedOrgVM.clearSelection()

    fakeUserRepo.newUser(user1)
    fakeUserRepo.newUser(user2)
    fakeUserRepo.newUser(user3)
    fakeUserRepo.newUser(fakeUser)
  }

  @Test
  fun allElementsAreDisplayed() = runTest {
    val org = Organization(id = "org1", name = "Test Organization")
    fakeOrgRepo.insertOrganization(org)
    fakeUserRepo.addAdminToOrganization(fakeUser.id, org.id)
    selectedOrgVM.selectOrganization(org.id)

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    composeTestRule.onNodeWithTag(OrganizationOverviewScreenTestTags.ROOT).assertIsDisplayed()

    // Organization image
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ORGANIZATION_IMAGE)
        .assertIsDisplayed()

    // Organization name
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ORGANIZATION_NAME_TEXT)
        .assertIsDisplayed()

    // Member count
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.MEMBER_COUNT_TEXT)
        .assertIsDisplayed()

    // Change organization button
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.CHANGE_ORGANIZATION_BUTTON)
        .assertIsDisplayed()

    // Edit organization button
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.EDIT_ORGANIZATION_BUTTON)
        .assertIsDisplayed()

    // Categories button
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.CATEGORIES_BUTTON)
        .assertIsDisplayed()

    // Invitations button
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.INVITATIONS_BUTTON)
        .assertIsDisplayed()

    // Members list
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.MEMBERS_LIST)
        .assertIsDisplayed()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun organizationDataAreDisplayed() = runTest {
    val org = Organization(id = "org1", name = "Test Organization")
    fakeOrgRepo.insertOrganization(org)
    fakeUserRepo.addUserToOrganization(user1.id, org.id)
    fakeUserRepo.addUserToOrganization(user2.id, org.id)
    fakeUserRepo.addUserToOrganization(user3.id, org.id)
    selectedOrgVM.selectOrganization(org.id)

    composeTestRule.setContent {
      OrganizationOverviewScreen(
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
        .assertTextEquals("3 members")
  }

  @Test
  fun clickOnEditOrganizationButtonCallsLambda() = runTest {
    val org = Organization(id = "org1", name = "Test Organization")
    fakeOrgRepo.insertOrganization(org)
    fakeUserRepo.addAdminToOrganization(fakeUser.id, org.id)
    selectedOrgVM.selectOrganization(org.id)

    var editClicked = false

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onEditOrganization = { editClicked = true })
    }

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.EDIT_ORGANIZATION_BUTTON)
        .performClick()

    assert(editClicked)
  }

  @Test
  fun clickOnCategoriesButtonCallsLambda() {
    var categoriesClicked = false

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onCategoriesClick = { categoriesClicked = true })
    }

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.CATEGORIES_BUTTON)
        .performClick()

    assert(categoriesClicked)
  }

  @Test
  fun clickOnInvitationsButtonCallsLambda() {
    var invitationsClicked = false
    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onInvitationClick = { invitationsClicked = true })
    }

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.INVITATIONS_BUTTON)
        .performClick()

    assert(invitationsClicked)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun clickingChangeButtonClearsOrganization() = runTest {
    val org = Organization(id = "org1", name = "Org")
    fakeOrgRepo.insertOrganization(org)
    selectedOrgVM.selectOrganization(org.id)

    var changeClicked = false

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onChangeOrganization = { changeClicked = true })
    }

    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.CHANGE_ORGANIZATION_BUTTON)
        .performClick()

    assert(selectedOrgVM.selectedOrganizationId.value == null)
    assert(changeClicked)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun snackBarAppearsOnError() = runTest {
    // Simulate an error in the ViewModel
    vm.setError(R.string.error_organization_not_found)

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    // Verify that the snackbar with the error message is displayed
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ERROR_SNACKBAR)
        .assertIsDisplayed()
  }

  @Test
  fun onMemberClickCallsLambda() = runTest {
    val org = Organization(id = "org1", name = "Test Organization")
    fakeOrgRepo.insertOrganization(org)
    fakeUserRepo.addUserToOrganization(user1.id, org.id)
    selectedOrgVM.selectOrganization(org.id)

    var memberClickedId: String? = null

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm,
          selectedOrganizationViewModel = selectedOrgVM,
          onMemberClick = { member -> memberClickedId = member.id })
    }

    composeTestRule.onNodeWithTag(MemberListTestTags.memberItemTag(user1.id)).performClick()

    assert(memberClickedId == user1.id)
  }

  @Test
  fun searchBarCorrectlyFiltersMembers() = runTest {
    val org = Organization(id = "org1", name = "Test Organization")
    fakeOrgRepo.insertOrganization(org)
    fakeUserRepo.addUserToOrganization(user1.id, org.id)
    fakeUserRepo.addUserToOrganization(user2.id, org.id)
    fakeUserRepo.addUserToOrganization(user3.id, org.id)
    selectedOrgVM.selectOrganization(org.id)

    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    // Enter search query
    composeTestRule
        .onNodeWithTag(MemberListTestTags.MEMBER_SEARCH_BAR)
        .performTextInput(user2.displayName!!)

    // Verify that only user2 is displayed
    composeTestRule.onNodeWithTag(MemberListTestTags.memberItemTag(user2.id)).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MemberListTestTags.memberItemTag(user1.id)).assertDoesNotExist()
    composeTestRule.onNodeWithTag(MemberListTestTags.memberItemTag(user3.id)).assertDoesNotExist()
  }

  @Test
  fun loadingIndicatorIsDisplayedWhileLoading() = runTest {
    vm.setLoading(true)
    composeTestRule.setContent {
      OrganizationOverviewScreen(
          organizationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    composeTestRule
        .onNodeWithTag(OrganizationOverviewScreenTestTags.ORG_DATA_LOADING_INDICATOR)
        .assertIsDisplayed()
  }
}
