package com.android.sample.ui.invitation

import androidx.credentials.Credential
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.repository.OrganizationRepository
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

  private val user = User(id = "user1", displayName = "Test User", email = "test@example.com")
  private val org =
      Organization(id = "org1", name = "Test Org", admins = listOf(user), members = listOf(user))
  private val inv1 = Invitation(id = "id1", organizationId = org.id, code = "123456")
  private val inv2 = Invitation(id = "id2", organizationId = org.id, code = "654321")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `showBottomSheet sets showBottomSheet to true`() = runTest {
    val vm = makeVm()
    assertFalse(vm.uiState.value.showBottomSheet)
    vm.showBottomSheet()
    assertTrue(vm.uiState.value.showBottomSheet)
  }

  @Test
  fun `dismissBottomSheet sets showBottomSheet to false`() = runTest {
    val vm = makeVm()
    vm.showBottomSheet()
    assertTrue(vm.uiState.value.showBottomSheet)
    vm.dismissBottomSheet()
    assertFalse(vm.uiState.value.showBottomSheet)
  }

  @Test
  fun `loadInvitations fetches invitations for organization`() = runTest {
    val vm = makeVm()
    vm.loadInvitations(org.id)
    testDispatcher.scheduler.advanceUntilIdle()
    val state = vm.uiState.value
    assertEquals(2, state.invitations.size)
    assertTrue(state.invitations.any { it.id == "id1" })
    assertTrue(state.invitations.any { it.id == "id2" })
    assertFalse(state.isLoading)
    assertNull(state.error)
  }

  @Test
  fun `deleteInvitation removes invitation from UI state`() = runTest {
    val vm = makeVm()
    vm.loadInvitations(org.id)
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(2, vm.uiState.value.invitations.size)

    vm.deleteInvitation("id1")
    testDispatcher.scheduler.advanceUntilIdle()
    val state = vm.uiState.value
    assertEquals(1, state.invitations.size)
    assertEquals("id2", state.invitations.first().id)
    assertNull(state.error)
  }

  @Test
  fun `deleteInvitation sets error when invitation not found`() = runTest {
    val vm = makeVm()
    vm.deleteInvitation("unknown-id")
    testDispatcher.scheduler.advanceUntilIdle()
    val state = vm.uiState.value
    assertNotNull(state.error)
    assertTrue(state.error!!.contains("Invitation with ID"))
  }

  /* Helper */
  private fun makeVm(): InvitationOverviewViewModel {
    val fakeInvitationRepository =
        object : InvitationRepository {
          private val invitations = mutableListOf(inv1, inv2)

          override suspend fun getAllInvitations(): List<Invitation> {
            return listOf(inv1, inv2)
          }

          override suspend fun getInvitationById(itemId: String): Invitation? {
            return when (itemId) {
              inv1.id -> inv1
              inv2.id -> inv2
              else -> null
            }
          }

          override suspend fun deleteInvitation(
              itemId: String,
              organization: Organization,
              user: User
          ) {
            val index = invitations.indexOfFirst { it.id == itemId }
            require(index != -1) { "Invitation with id $itemId does not exist." }
            invitations.removeAt(index)
          }
        }

    val fakeAuthRepository =
        object : AuthRepository {
          override suspend fun signInWithGoogle(credential: Credential): Result<User> {
            return Result.success(user)
          }

          override fun signOut(): Result<Unit> {

            return Result.success(Unit)
          }

          override fun getCurrentUser(): User? {
            return user
          }

          override suspend fun getUserById(userId: String): User? {
            return user
          }
        }

    val fakeOrganizationRepository =
        object : OrganizationRepository {
          override suspend fun getAllOrganizations(user: User): List<Organization> {
            return listOf(org)
          }

          override suspend fun deleteOrganization(organizationId: String, user: User) {}

          override suspend fun getOrganizationById(
              organizationId: String,
              user: User
          ): Organization? {
            return org
          }
        }
    return InvitationOverviewViewModel(
        invitationRepository = fakeInvitationRepository,
        organizationRepository = fakeOrganizationRepository,
        authRepository = fakeAuthRepository)
  }
}
