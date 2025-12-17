package com.android.sample.ui.organization

import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.network.FakeConnectivityChecker
import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkTestBase
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OrganizationViewModel.
 *
 * Tests the ViewModel's core functionality including loading and refreshing organizations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrganizationViewModelTest : NetworkTestBase {

  override val fakeChecker = FakeConnectivityChecker(state = true)
  override val networkRepo = NetworkStatusRepository(fakeChecker)

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var organizationRepository: OrganizationRepository
  private lateinit var userRepository: UsersRepositoryLocal
  private lateinit var authRepository: AuthRepository
  private lateinit var viewModel: OrganizationViewModel

  private val testUser = User(id = "user1", displayName = "Test User", email = "test@example.com")
  private val testOrg1 = Organization(id = "org1", name = "Organization 1")
  private val testOrg2 = Organization(id = "org2", name = "Organization 2")

  @Before
  fun setUp() {
    setupNetworkTestBase()

    Dispatchers.setMain(testDispatcher)

    // Create mock repositories
    organizationRepository = mockk()
    authRepository = mockk()
    userRepository = mockk()

    // Setup default mock behavior
    coEvery { authRepository.getCurrentUser() } returns testUser
    coEvery { organizationRepository.getAllOrganizations(testUser) } returns
        listOf(testOrg1, testOrg2)

    viewModel = OrganizationViewModel(organizationRepository, authRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateShouldBeLoading() = runTest {
    val newViewModel = OrganizationViewModel(organizationRepository, authRepository)
    val state = newViewModel.uiState.value
    assertTrue(state.isLoading)
  }

  @Test
  fun loadOrganizationsShouldLoadOrganizationsIntoUiState() = runTest {
    // Advance dispatcher to execute loadOrganizations from init
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(2, state.organizations.size)
    assertTrue(state.organizations.any { it.name == "Organization 1" })
    assertTrue(state.organizations.any { it.name == "Organization 2" })
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
  }

  @Test
  fun refreshOrganizationsShouldSetRefreshingStateAndLoadNewData() = runTest {
    // First, let initial load complete
    testDispatcher.scheduler.advanceUntilIdle()

    // Update mock to return different data
    val updatedOrg = Organization(id = "org3", name = "Updated Organization")
    coEvery { organizationRepository.getAllOrganizations(testUser) } returns listOf(updatedOrg)

    // Call refresh and complete it
    viewModel.refreshOrganizations()
    testDispatcher.scheduler.advanceUntilIdle()

    // Check final state
    val state = viewModel.uiState.value
    assertFalse(state.isRefreshing)
    assertEquals(1, state.organizations.size)
    assertEquals("Updated Organization", state.organizations.first().name)
    assertNull(state.errorMsg)
  }

  @Test
  fun refreshOrganizationsShouldHandleErrorsGracefully() = runTest {
    // First, let initial load complete
    testDispatcher.scheduler.advanceUntilIdle()

    // Setup mock to throw exception
    val errorMessage = "Network error"
    coEvery { organizationRepository.getAllOrganizations(testUser) } throws Exception(errorMessage)

    // Call refresh
    viewModel.refreshOrganizations()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isRefreshing)
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Failed to refresh organizations"))
  }

  @Test
  fun clearErrorMsgShouldResetErrorMsg() = runTest {
    testDispatcher.scheduler.advanceUntilIdle()

    // Setup error scenario
    coEvery { organizationRepository.getAllOrganizations(testUser) } throws Exception("Error")
    viewModel.refreshOrganizations()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify error is set
    assertNotNull(viewModel.uiState.value.errorMsg)

    // Clear error
    viewModel.clearErrorMsg()

    // Verify error is cleared
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun refreshOrganizationsShouldCallRepositoryWithCorrectUser() = runTest {
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.refreshOrganizations()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify repository was called with correct user (at least twice: init + refresh)
    coVerify(atLeast = 2) { organizationRepository.getAllOrganizations(testUser) }
  }

  @Test
  fun refreshOrganizationsShouldNotChangeLoadingState() = runTest {
    testDispatcher.scheduler.advanceUntilIdle()

    // Ensure initial loading is false
    assertFalse(viewModel.uiState.value.isLoading)

    viewModel.refreshOrganizations()
    testDispatcher.scheduler.advanceUntilIdle()

    // Loading should still be false (only refreshing should be used)
    assertFalse(viewModel.uiState.value.isLoading)
  }
}
