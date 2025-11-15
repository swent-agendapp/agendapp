package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organizations.Organization
import com.android.sample.model.organizations.OrganizationRepository
import com.android.sample.model.organizations.OrganizationRepositoryProvider
import com.github.se.bootcamp.model.authentication.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizationUIState(
    val isLoading: Boolean = true,
    val organizations: List<Organization> = emptyList(),
    val selectedOrganization: Organization? = null,
    val errorMsg: String? = null
)

// ViewModel for managing organization data for the current user
class OrganizationViewModel(
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  // State holding the UI state of the organizations of the current user
  private val _uiState = MutableStateFlow(OrganizationUIState())
  val uiState: StateFlow<OrganizationUIState> = _uiState

  // Initialize by loading organizations
  init {
    loadOrganizations()
  }

  // Load organizations for the current user
  private fun loadOrganizations() {
    viewModelScope.launch {
      // Get the current authenticated user
      val user =
          authRepository.getCurrentUser()
              ?: throw IllegalStateException("No authenticated user found.")

      // Update UI state to loading and fetch organizations
      _uiState.update { it.copy(isLoading = true) }
      _uiState.update {
        it.copy(organizations = organizationRepository.getAllOrganizations(user), isLoading = false)
      }
    }
  }

  // Clear any error message in the UI state
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  // Handle organization selection
  fun selectOrganization(organization: Organization) {

    // Ensure the selected organization is in the user's organization list
    if (!_uiState.value.organizations.contains(organization)) {
      throw IllegalArgumentException(
          "Selected organization is not in the user's organization list.")
    }

    // Update the selected organization in the UI state
    _uiState.update { it.copy(selectedOrganization = organization) }
  }
}
