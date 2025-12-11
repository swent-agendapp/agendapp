package com.android.sample.ui.invitation

import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.FakeInvitationRepository
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InvitationOverviewViewModelTest {

  // Single dispatcher used for Dispatchers.Main AND for advancing in tests
  private val testDispatcher = StandardTestDispatcher()

  private lateinit var fakeInvitationRepository: FakeInvitationRepository
  private lateinit var fakeAuthRepository: FakeAuthRepository
  private lateinit var vm: InvitationOverviewViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var organizationRepository: OrganizationRepository

  private val user = User(id = "user1", displayName = "Test User", email = "test@example.com")
  private val org1 = Organization(id = "org1", name = "Test Org 1")
  private val org2 = Organization(id = "org2", name = "Test Org 2")
  private val inv1 = Invitation(id = "id1", organizationId = org1.id, code = "123456")
  private val inv2 = Invitation(id = "id2", organizationId = org1.id, code = "654321")
  private val inv3 = Invitation(id = "id3", organizationId = org2.id, code = "132435")

  @Before
  fun setUp() = runBlocking {
    // IMPORTANT: install Main BEFORE creating the ViewModel
    Dispatchers.setMain(testDispatcher)

    userRepository = UsersRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal(userRepository)

    // Register all users in the repository
    userRepository.newUser(user)

    // Create organizations
    organizationRepository.insertOrganization(org1)
    organizationRepository.insertOrganization(org2)

    // Set up user-organization relationships
    userRepository.addAdminToOrganization(user.id, org1.id)

    // Fake invitation repo + initial data
    fakeInvitationRepository = FakeInvitationRepository(userRepository)
    fakeInvitationRepository.addInvitation(inv1)
    fakeInvitationRepository.addInvitation(inv2)
    fakeInvitationRepository.addInvitation(inv3)

    // Fake auth repo with logged-in user
    fakeAuthRepository = FakeAuthRepository(user)

    // ViewModel under test – after Main is set
    vm =
      InvitationOverviewViewModel(
        invitationRepository = fakeInvitationRepository,
        organizationRepository = organizationRepository,
        authRepository = fakeAuthRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `showBottomSheet sets showBottomSheet to true`() = runTest {
    assertFalse(vm.uiState.value.showBottomSheet)
    vm.showBottomSheet()
    assertTrue(vm.uiState.value.showBottomSheet)
  }

  @Test
  fun `dismissBottomSheet sets showBottomSheet to false`() = runTest {
    vm.showBottomSheet()
    assertTrue(vm.uiState.value.showBottomSheet)
    vm.dismissBottomSheet()
    assertFalse(vm.uiState.value.showBottomSheet)
  }

  @Test
  fun `loadInvitations fetches invitations for organization`() = runTest {
    vm.loadInvitations(org1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(2, state.invitations.size)
    assertTrue(state.invitations.any { it.id == "id1" })
    assertTrue(state.invitations.any { it.id == "id2" })
    assertFalse(state.isLoading)
    assertNull(state.errorMessageId)
  }

  @Test
  fun `load invitation without authenticated user sets error message`() = runTest {
    fakeAuthRepository.clearCurrentUser()
    vm.loadInvitations(org1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(R.string.error_no_authenticated_user, state.errorMessageId)
  }

  @Test
  fun `load invitation sets error message when organization does not exist`() = runTest {
    organizationRepository.deleteOrganization(org1.id, user)
    vm.loadInvitations(org1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(R.string.error_organization_not_found, state.errorMessageId)
  }

  @Test
  fun `admin user can add invitation`() = runTest {
    vm.addInvitations(org1.id, 2)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(4, state.invitations.size)
  }

  @Test
  fun `adding invitation without authenticated user sets error message`() = runTest {
    fakeAuthRepository.clearCurrentUser()
    vm.addInvitations(org1.id, 1)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(R.string.error_no_authenticated_user, state.errorMessageId)
  }

  @Test
  fun `adding invitation sets error message when organization does not exist`() = runTest {
    organizationRepository.deleteOrganization(org1.id, user)
    vm.addInvitations(org1.id, 1)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(R.string.error_organization_not_found, state.errorMessageId)
  }

  @Test
  fun `deleteInvitation removes invitation from UI state`() = runTest {
    vm.loadInvitations(org1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    vm.deleteInvitation(inv1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(1, state.invitations.size)
    assertEquals(inv2.id, state.invitations.first().id)
    assertNull(state.errorMessageId)
  }

  @Test
  fun `deleteInvitation without authenticated user sets error message`() = runTest {
    // first load with an authenticated user otherwise load fails
    vm.loadInvitations(org1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    fakeAuthRepository.clearCurrentUser()
    vm.deleteInvitation(inv1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(2, state.invitations.size)
    assertEquals(R.string.error_no_authenticated_user, state.errorMessageId)
  }

  @Test
  fun `deleteInvitation sets error when invitation sets error message`() = runTest {
    vm.deleteInvitation("unknown-id")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertNotNull(state.errorMessageId)
  }

  @Test
  fun `deleteInvitation sets error message when organization does not exist`() = runTest {
    organizationRepository.deleteOrganization(org1.id, user)
    vm.deleteInvitation(inv1.id)
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(R.string.error_organization_not_found, state.errorMessageId)
  }

  @Test
  fun `setInvitations correctly sets`() = runTest {
    val newInvitationList = listOf(inv2)
    vm.setInvitations(newInvitationList)

    val state = vm.uiState.value
    assertEquals(state.invitations, newInvitationList)
  }

  @Test
  fun `setError correctly sets`() = runTest {
    val newError = 1
    vm.setError(newError)

    val state = vm.uiState.value
    assertEquals(state.errorMessageId, newError)
  }

  @Test
  fun `setLoading correctly sets to true`() = runTest {
    vm.setLoading(true)

    val state = vm.uiState.value
    assertTrue(state.isLoading)
  }

  @Test
  fun `setLoading correctly sets to false`() = runTest {
    vm.setLoading(false)

    val state = vm.uiState.value
    assertFalse(state.isLoading)
  }
}