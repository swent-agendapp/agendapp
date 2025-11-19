package com.android.sample.ui.organization

import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.Organization
import com.android.sample.model.organization.OrganizationRepositoryLocal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake ViewModel for OrganizationListScreen tests. Uses FakeAuthRepository and
 * FakeOrganizationRepository.
 */
class FakeOrganizationViewModel(
    fakeUser: User = User(id = "1", displayName = "Test User", email = "test@example.com")
) :
    OrganizationViewModel(
        organizationRepository = OrganizationRepositoryLocal(),
        authRepository = FakeAuthRepository(fakeUser)) {

  private val _uiState = MutableStateFlow(OrganizationUIState())
  override val uiState: StateFlow<OrganizationUIState> = _uiState

  /** Simulate loading state */
  fun setLoading() {
    _uiState.value = OrganizationUIState(isLoading = true)
  }

  /** Simulate an error message */
  fun setError(message: String) {
    _uiState.value = OrganizationUIState(isLoading = false, errorMsg = message)
  }

  /** Simulate organizations loaded */
  fun setOrganizations(organizations: List<Organization>) {
    _uiState.value = OrganizationUIState(isLoading = false, organizations = organizations)
  }

  /** Simulate selecting an organization */
  override fun selectOrganization(organization: Organization) {
    val currentOrgs = _uiState.value.organizations
    require(currentOrgs.contains(organization)) {
      "Selected organization is not in the current list."
    }
    _uiState.value = _uiState.value.copy(selectedOrganization = organization)
  }

  /** Clear error message */
  override fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
}
