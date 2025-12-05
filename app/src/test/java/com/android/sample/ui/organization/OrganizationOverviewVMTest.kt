package com.android.sample.ui.organization

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.R
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizationOverviewViewModelTest {

  private lateinit var authRepository: FakeAuthRepository
  private lateinit var organizationRepository: FakeOrganizationRepository
  private lateinit var vm: OrganizationOverviewViewModel

  private val fakeUser = User(id = "user1", email = "test@test.com", displayName = "Tester")

  // Users for organization members
  private val user1 = User(id = "user1", email = "user1@test.com")
  private val user2 = User(id = "user2", email = "user2@test.com")
  private val user3 = User(id = "user3", email = "user3@test.com")

  @Before
  fun setup() {
    authRepository = FakeAuthRepository(fakeUser)
    organizationRepository = FakeOrganizationRepository()
    vm = OrganizationOverviewViewModel(organizationRepository, authRepository)

    // Clear any selected organization before each test
    SelectedOrganizationRepository.clearSelection()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `fillSelectedOrganizationDetails fills UI state correctly`() = runTest {
    val orgId = "org1"
    val org =
        Organization(id = orgId, name = "My Organization", members = listOf(user1, user2, user3))
    organizationRepository.insertOrganization(org)

    vm.fillSelectedOrganizationDetails(orgId)

    val state = vm.uiState.value
    assertEquals("My Organization", state.organizationName)
    assertEquals(3, state.memberCount)
    assertNull(state.errorMessageId)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `fillSelectedOrganizationDetails sets error when no organization id`() = runTest {
    vm.fillSelectedOrganizationDetails("")

    val state = vm.uiState.value
    assertEquals(R.string.error_no_organization_selected, state.errorMessageId)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `fillSelectedOrganizationDetails sets error when no authenticated user`() = runTest {
    val vmNoUser = OrganizationOverviewViewModel(organizationRepository, FakeAuthRepository(null))
    vmNoUser.fillSelectedOrganizationDetails("org1")

    val state = vmNoUser.uiState.value
    assertEquals(R.string.error_no_authenticated_user, state.errorMessageId)
  }

  @Test
  fun `clearSelectedOrganization resets UI state`() {
    // Simulate past existing state
    vm.clearSelectedOrganization()

    val state = vm.uiState.value
    assertEquals("", state.organizationName)
    assertEquals(0, state.memberCount)
    assertNull(state.errorMessageId)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `deleteSelectedOrganization removes org and clears selection`() = runTest {
    val orgId = "orgToDelete"
    val org = Organization(id = orgId, name = "Deletable Org", members = listOf(user1))
    organizationRepository.insertOrganization(org)

    vm.deleteSelectedOrganization(orgId)

    assertNull(organizationRepository.getOrganizationById(orgId, fakeUser))

    val state = vm.uiState.value
    assertEquals("", state.organizationName)
    assertEquals(0, state.memberCount)
    assertNull(state.errorMessageId)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `deleteSelectedOrganization sets error on null id`() = runTest {
    vm.deleteSelectedOrganization(null)

    val state = vm.uiState.value
    assertEquals(R.string.error_no_organization_to_delete, state.errorMessageId)
  }
}
