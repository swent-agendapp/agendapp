package com.android.sample.ui.invitation

import androidx.credentials.Credential
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryLocal
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.ui.invitation.createInvitation.CreateInvitationViewModel
import com.android.sample.ui.invitation.createInvitation.INVALID_INVITATION_COUNT_ERROR_MSG
import com.android.sample.ui.organization.SelectedOrganizationViewModel
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
class CreateInvitationViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var invitationRepository: InvitationRepository
  private val admin = User(id = "admin1", displayName = "Admin User", email = "")
  private val employee = User(id = "emp1", displayName = "Employee User", email = "")
  private val organization: Organization =
      Organization(
          id = "org1",
          name = "Super Organization",
          admins = listOf(admin),
          members = listOf(employee, admin),
      )

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    invitationRepository = InvitationRepositoryLocal()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial UI state has count 1`() {
    val vm = makeVm(true)
    val state = vm.uiState.value
    assertEquals(1, state.count)
  }

  @Test
  fun `setCount sets the count to specified value`() {
    val vm = makeVm(true)
    vm.setCount(5)
    assertEquals(5, vm.uiState.value.count)

    vm.setCount(1)
    assertEquals(1, vm.uiState.value.count)
  }

  @Test
  fun `setCount with invalid values sets error message and does not change the count`() {
    val vm = makeVm(true)
    vm.setCount(-1)
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
    assertEquals(1, vm.uiState.value.count)
    vm.setCount(150)
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
    assertEquals(1, vm.uiState.value.count)
  }

  @Test
  fun `increment increases count by 1 but never higher than 99`() {
    val vm = makeVm(true)
    vm.increment()
    assertEquals(2, vm.uiState.value.count)

    vm.increment()
    assertEquals(3, vm.uiState.value.count)

    vm.setCount(99)
    assertEquals(99, vm.uiState.value.count)
    vm.increment()
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
  }

  @Test
  fun `decrement decreases count by 1 but never below 0`() {
    val vm = makeVm(true)
    vm.decrement()
    assertEquals(1, vm.uiState.value.count)

    vm.setCount(3)
    vm.decrement()
    assertEquals(2, vm.uiState.value.count)
    vm.decrement()
    assertEquals(1, vm.uiState.value.count)

    vm.decrement()
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
  }

  @Test
  fun `admin user can add invitation`() = runTest {
    val vm = makeVm(true)

    vm.increment()
    assertEquals(2, vm.uiState.value.count)
    vm.addInvitations()

    testDispatcher.scheduler.advanceUntilIdle()
    val invitations = invitationRepository.getAllInvitations()
    assertEquals(2, invitations.size)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `non-admin user cannot add invitation`() = runTest {
    val vm = makeVm(false)

    vm.increment()
    assertEquals(2, vm.uiState.value.count)
    vm.addInvitations()
  }

  @Test(expected = IllegalStateException::class)
  fun `adding invitation without authenticated user throws exception`() = runTest {
    val vm = makeVm(isAdminVm = false, hasAuthenticatedUser = false)
    vm.addInvitations()
  }

  @Test(expected = IllegalStateException::class)
  fun `adding invitation without selected organization throws exception`() = runTest {
    val vm = makeVm(isAdminVm = false, hasSelectedOrganization = false)
    vm.addInvitations()
  }

  @Test(expected = IllegalStateException::class)
  fun `adding invitation when selected organization does not exist throws exception`() = runTest {
    val vm = makeVm(isAdminVm = false, selectedOrganizationExists = false)
    vm.addInvitations()
  }

  @Test
  fun `canCreateInvitations returns false when errorMsg is not null`() {
    val vm = makeVm(hasAuthenticatedUser = false)
    vm.setCount(-5) // Invalid count to set error message
    assertFalse(vm.canCreateInvitations())
    assertEquals(INVALID_INVITATION_COUNT_ERROR_MSG, vm.uiState.value.errorMsg)
  }

  private fun makeVm(
      isAdminVm: Boolean = false,
      hasAuthenticatedUser: Boolean = true,
      hasSelectedOrganization: Boolean = true,
      selectedOrganizationExists: Boolean = true
  ): CreateInvitationViewModel {
    val fakeAuthRepository =
        object : AuthRepository {
          override suspend fun signInWithGoogle(credential: Credential): Result<User> {
            return Result.success(if (isAdminVm) admin else employee)
          }

          override fun signOut(): Result<Unit> {

            return Result.success(Unit)
          }

          override fun getCurrentUser(): User? {
            return if (!hasAuthenticatedUser) null else if (isAdminVm) admin else employee
          }

          override suspend fun getUserById(userId: String): User? {
            return when (userId) {
              admin.id -> {
                admin
              }
              employee.id -> {
                employee
              }
              else -> {
                null
              }
            }
          }
        }
    val selectedOrganizationVm = SelectedOrganizationViewModel()
    if (hasSelectedOrganization) {
      selectedOrganizationVm.selectOrganization(organization.id)
    } else {
      selectedOrganizationVm.clearSelection()
    }
    val fakeOrganizationRepository =
        object : OrganizationRepository {
          override suspend fun getAllOrganizations(user: User): List<Organization> {
            return listOf(organization)
          }

          override suspend fun deleteOrganization(organizationId: String, user: User) {}

          override suspend fun getOrganizationById(
              organizationId: String,
              user: User
          ): Organization? {
            return if (selectedOrganizationExists) organization else null
          }
        }
    return CreateInvitationViewModel(
        invitationRepository = invitationRepository,
        authRepository = fakeAuthRepository,
        organizationRepository = fakeOrganizationRepository,
        selectedOrganizationViewModel = selectedOrganizationVm)
  }
}
