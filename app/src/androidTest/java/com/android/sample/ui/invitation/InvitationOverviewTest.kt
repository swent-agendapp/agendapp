package com.android.sample.ui.invitation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.FakeInvitationRepository
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.ui.invitation.createInvitation.InvitationCreationTestTags
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import com.android.sample.utils.FirebaseEmulatedTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests written by AI

class InvitationOverviewScreenTest : FirebaseEmulatedTest() {

  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var fakeInvitationRepository: FakeInvitationRepository
  private lateinit var fakeOrganizationRepository: FakeOrganizationRepository
  private lateinit var fakeAuthRepository: FakeAuthRepository
  private lateinit var vm: InvitationOverviewViewModel
  private lateinit var selectedOrgVM: SelectedOrganizationViewModel

  private val user = User(id = "user1", displayName = "Test User", email = "test@example.com")
  private val org = Organization(id = "org1", name = "Test Org")
  private val inv1 = Invitation(id = "id1", organizationId = org.id, code = "123456")
  private val inv2 = Invitation(id = "id2", organizationId = org.id, code = "654321")

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  override fun setUp() = runBlocking {
    fakeOrganizationRepository = FakeOrganizationRepository()
    fakeOrganizationRepository.insertOrganization(org)

    fakeInvitationRepository = FakeInvitationRepository()
    fakeInvitationRepository.addInvitation(inv1)
    fakeInvitationRepository.addInvitation(inv2)

    fakeAuthRepository = FakeAuthRepository(user)
    vm =
        InvitationOverviewViewModel(
            invitationRepository = fakeInvitationRepository,
            organizationRepository = fakeOrganizationRepository,
            authRepository = fakeAuthRepository)
    selectedOrgVM = SelectedOrganizationVMProvider.viewModel
    selectedOrgVM.selectOrganization(org.id)
  }
  // -------------------------------------------------------
  // ROOT + BASIC STRUCTURE
  // -------------------------------------------------------
  @Test
  fun root_isDisplayed() {
    composeTestRule.setContent {
      InvitationOverviewScreen(
          invitationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    composeTestRule.onNodeWithTag(InvitationOverviewScreenTestTags.ROOT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.INVITATION_LIST)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // TOP BAR
  // -------------------------------------------------------
  @Test
  fun topBar_displaysCorrectTitle() {
    composeTestRule.setContent {
      InvitationOverviewScreen(
          invitationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    composeTestRule.onNodeWithTag(InvitationOverviewScreenTestTags.TITLE).assertIsDisplayed()
  }

  // -------------------------------------------------------
  // FAB BUTTON
  // -------------------------------------------------------
  @Test
  fun createInvitation_fab_isDisplayed() {
    composeTestRule.setContent {
      InvitationOverviewScreen(
          invitationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // OPEN BOTTOM SHEET
  // -------------------------------------------------------
  @Test
  fun clickingCreateInvitation_opensBottomSheet() {
    composeTestRule.setContent {
      InvitationOverviewScreen(
          invitationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    // Bottom sheet should NOT exist at first
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET)
        .assertDoesNotExist()

    // Click floating action button
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON)
        .performClick()
    // Now bottom sheet is visible
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET)
        .assertIsDisplayed()
  }

  // -------------------------------------------------------
  // DISMISS BOTTOM SHEET
  // -------------------------------------------------------
  @Test
  fun cancelInsideSheet_hidesBottomSheet() {
    composeTestRule.setContent {
      InvitationOverviewScreen(
          invitationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }
    // Open sheet
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BUTTON)
        .performClick()

    // Wait for sheet contents
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.CANCEL_BUTTON).assertIsDisplayed()

    // Click CANCEL
    composeTestRule.onNodeWithTag(InvitationCreationTestTags.CANCEL_BUTTON).performClick()

    // After cancel → sheet disappears
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.CREATE_INVITATION_BOTTOM_SHEET)
        .assertDoesNotExist()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun snackBarAppearsOnError() = runTest {
    // Simulate an error in the ViewModel
    vm.setError(R.string.error_invitation_not_found)

    composeTestRule.setContent {
      InvitationOverviewScreen(
          invitationOverviewViewModel = vm, selectedOrganizationViewModel = selectedOrgVM)
    }

    // Verify that the snackbar with the error message is displayed
    composeTestRule
        .onNodeWithTag(InvitationOverviewScreenTestTags.INVITATION_ERROR_SNACKBAR)
        .assertIsDisplayed()
  }
}
