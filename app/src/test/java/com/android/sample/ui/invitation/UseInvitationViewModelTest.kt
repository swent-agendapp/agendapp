package com.android.sample.ui.invitation.useInvitation

import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.invitation.FakeInvitationRepository
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
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
class UseInvitationViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeInvitationRepository: FakeInvitationRepository
  private lateinit var fakeOrganizationRepository: FakeOrganizationRepository
  private lateinit var fakeAuthRepository: FakeAuthRepository
  private lateinit var vm: UseInvitationViewModel

  private val user = User("user1", "Tester", "test@example.com")
  private val organization =
      Organization(id = "org1", name = "Org", admins = listOf(user), members = emptyList())

  private val validInvitation =
      Invitation(
          id = "inv1",
          organizationId = organization.id,
          code = "ABC123",
          status = InvitationStatus.Active)

  private val usedInvitation =
      validInvitation.copy(id = "inv2", code = "USED12", status = InvitationStatus.Used)

  private val expiredInvitation =
      validInvitation.copy(id = "inv3", code = "EXP999", status = InvitationStatus.Expired)

  @Before
  fun setup() {
    fakeInvitationRepository = FakeInvitationRepository()
    fakeInvitationRepository.addInvitation(validInvitation)
    fakeInvitationRepository.addInvitation(usedInvitation)
    fakeInvitationRepository.addInvitation(expiredInvitation)

    fakeOrganizationRepository = FakeOrganizationRepository()
    fakeOrganizationRepository.addOrganization(organization)

    fakeAuthRepository = FakeAuthRepository(user)

    vm =
        UseInvitationViewModel(
            invitationRepository = fakeInvitationRepository,
            organizationRepository = fakeOrganizationRepository,
            authRepository = fakeAuthRepository)

    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // -----------------------------------------------------------
  // setCode
  // -----------------------------------------------------------

  @Test
  fun `setCode sanitizes input`() = runTest {
    vm.setCode("a!b@c#1$2%3")
    assertEquals("ABC123", vm.uiState.value.code)
  }

  @Test
  fun `setCode uppercases and truncates`() = runTest {
    vm.setCode("abcd1234efg")
    assertEquals("ABCD12", vm.uiState.value.code)
  }

  // -----------------------------------------------------------
  // joinWithCode
  // -----------------------------------------------------------

  @Test
  fun `joinWithCode sets error when no authenticated user`() = runTest {
    fakeAuthRepository.clearCurrentUser()
    vm.setCode("ABC123")
    vm.joinWithCode()

    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(R.string.error_no_authenticated_user, vm.uiState.value.errorMessageId)
    assertFalse(vm.uiState.value.isTemptingToJoin)
  }

  @Test
  fun `joinWithCode sets error when invitation code not found`() = runTest {
    vm.setCode("ZZZZZZ")
    vm.joinWithCode()

    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(R.string.error_invitation_with_code_not_found, vm.uiState.value.errorMessageId)
  }

  @Test
  fun `joinWithCode sets error when invitation is already used`() = runTest {
    vm.setCode(usedInvitation.code)
    vm.joinWithCode()

    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(R.string.error_invitation_already_used, vm.uiState.value.errorMessageId)
  }

  @Test
  fun `joinWithCode sets error when invitation is expired`() = runTest {
    vm.setCode(expiredInvitation.code)
    vm.joinWithCode()

    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(R.string.error_invitation_expired, vm.uiState.value.errorMessageId)
  }

  @Test
  fun `joinWithCode sets error when organization addMember fails`() = runTest {
    fakeOrganizationRepository.deleteOrganization(organization.id, user)

    vm.setCode(validInvitation.code)
    vm.joinWithCode()

    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(R.string.error_joining_organization, vm.uiState.value.errorMessageId)
  }

  @Test
  fun `joinWithCode updates invitation to Used on success`() = runTest {
    vm.setCode(validInvitation.code)
    vm.joinWithCode()

    testDispatcher.scheduler.advanceUntilIdle()

    val updated = fakeInvitationRepository.getInvitationById(validInvitation.id)
    assertEquals(InvitationStatus.Used, updated?.status)
    assertNull(vm.uiState.value.errorMessageId)
    assertFalse(vm.uiState.value.isTemptingToJoin)
  }

  // -----------------------------------------------------------
  // setError
  // -----------------------------------------------------------

  @Test
  fun `setError sets error message`() = runTest {
    vm.setError(123)
    assertEquals(123, vm.uiState.value.errorMessageId)
  }

  @Test
  fun `setError clears error when null`() = runTest {
    vm.setError(null)
    assertNull(vm.uiState.value.errorMessageId)
  }

  // -----------------------------------------------------------
  // setIsTemptingToJoin
  // -----------------------------------------------------------

  @Test
  fun `setIsTemptingToJoin updates state`() = runTest {
    vm.setIsTemptingToJoin(true)
    assertTrue(vm.uiState.value.isTemptingToJoin)

    vm.setIsTemptingToJoin(false)
    assertFalse(vm.uiState.value.isTemptingToJoin)
  }


  // -----------------------------------------------------------
  // setIsInputCodeIllegal
  // -----------------------------------------------------------
  @Test
  fun `setIsInputCodeIllegal updates isIllegal`() = runTest {
    vm.setIsInputCodeIllegal(true)
    assertTrue(vm.uiState.value.isInputCodeIllegal)

    vm.setIsInputCodeIllegal(false)
    assertFalse(vm.uiState.value.isInputCodeIllegal)

  }
}
