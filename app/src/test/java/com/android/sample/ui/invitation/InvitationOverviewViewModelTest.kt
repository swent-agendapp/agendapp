package com.android.sample.ui.invitation

import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.FakeInvitationRepository
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeInvitationRepository: FakeInvitationRepository
  private lateinit var fakeOrganizationRepository: FakeOrganizationRepository
  private lateinit var fakeAuthRepository: FakeAuthRepository
  private lateinit var vm: InvitationOverviewViewModel
  private val user = User(id = "user1", displayName = "Test User", email = "test@example.com")
  private val org =
      Organization(id = "org1", name = "Test Org", admins = listOf(user), members = listOf(user))
  private val inv1 = Invitation(id = "id1", organizationId = org.id, code = "123456")
  private val inv2 = Invitation(id = "id2", organizationId = org.id, code = "654321")

  @Before
  fun setUp() {
    fakeOrganizationRepository = FakeOrganizationRepository()
    fakeOrganizationRepository.addOrganization(org)

    fakeInvitationRepository = FakeInvitationRepository()
    fakeInvitationRepository.addInvitation(inv1)
    fakeInvitationRepository.addInvitation(inv2)

    fakeAuthRepository = FakeAuthRepository(user)
    vm =
        InvitationOverviewViewModel(
            invitationRepository = fakeInvitationRepository,
            organizationRepository = fakeOrganizationRepository,
            authRepository = fakeAuthRepository)
    Dispatchers.setMain(testDispatcher)
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
    vm.loadInvitations(org.id)
    testDispatcher.scheduler.advanceUntilIdle()
    val state = vm.uiState.value
    assertEquals(2, state.invitations.size)
    assertTrue(state.invitations.any { it.id == "id1" })
    assertTrue(state.invitations.any { it.id == "id2" })
    assertFalse(state.isLoading)
    assertNull(state.errorMessageId)
  }

  @Test
  fun `deleteInvitation removes invitation from UI state`() = runTest {
    vm.loadInvitations(org.id)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(2, vm.uiState.value.invitations.size)

    vm.deleteInvitation("id1")
    testDispatcher.scheduler.advanceUntilIdle()
    val state = vm.uiState.value
    assertEquals(1, state.invitations.size)
    assertEquals("id2", state.invitations.first().id)
    assertNull(state.errorMessageId)
  }

  @Test
  fun `deleteInvitation sets error when invitation not found`() = runTest {
    vm.deleteInvitation("unknown-id")
    testDispatcher.scheduler.advanceUntilIdle()
    val state = vm.uiState.value
    assertNotNull(state.errorMessageId)
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
