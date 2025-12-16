package com.android.sample.ui.organization

import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkStatusRepositoryProvider
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

/**
 * Fake ViewModel for OrganizationListScreen tests. Uses FakeAuthRepository and
 * FakeOrganizationRepository.
 */
class FakeOrganizationViewModel(
    fakeUser: User = User(id = "1", displayName = "Test User", email = "test@example.com"),
    networkStatusRepository: NetworkStatusRepository = NetworkStatusRepositoryProvider.repository,
    private val orgRepository: OrganizationRepository = FakeOrganizationRepository()
) :
    OrganizationViewModel(
        organizationRepository = orgRepository,
        authRepository = FakeAuthRepository(fakeUser),
        networkStatusRepository = networkStatusRepository) {

  // Override init to avoid loading organizations for tests
  init {
    observeNetworkStatus()
  }

  /** Simulate loading state */
  fun setLoading() {
    _uiState.update { it.copy(isLoading = true) }
  }

  /** Simulate an error message */
  fun setError(message: String) {
    _uiState.update { it.copy(isLoading = false, errorMsg = message) }
  }

  /** Simulate organizations loaded */
  fun setOrganizations(organizations: List<Organization>, userId: String) {
    // Clear existing organizations and insert new ones
    orgRepository.apply {
      runBlocking {
        getAllOrganizations(User(userId)).forEach { deleteOrganization(it.id, User(userId)) }
        organizations.forEach { insertOrganization(it) }
      }
    }

    // Update UI state
    _uiState.update { it.copy(isLoading = false, organizations = organizations) }
  }

  /** Simulate refreshing state */
  fun setRefreshing(refreshing: Boolean = true) {
    _uiState.update { it.copy(isRefreshing = refreshing) }
  }

  override fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }
}
