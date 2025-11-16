package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.Organization
import com.android.sample.model.organization.OrganizationRepository
import com.android.sample.model.organization.OrganizationRepositoryProvider
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
    authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  // State holding the UI state of the organizations of the current user
  private val _uiState = MutableStateFlow(OrganizationUIState())
  val uiState: StateFlow<OrganizationUIState> = _uiState

  // State holding the current user
  private val _userState = MutableStateFlow(authRepository.getCurrentUser())
  val userState: StateFlow<User?> = _userState

  // Initialize by loading organizations
  init {
    loadOrganizations()
  }

  // Load organizations for the current user
  private fun loadOrganizations() {
    viewModelScope.launch {
      // Get the current authenticated user
      val user = userState.value ?: throw IllegalStateException("No authenticated user found.")

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

  // Add a new organization with the given name for the current user (himself as the only admin and
  // member)
  fun addOrganizationFromName(name: String) {
    viewModelScope.launch {
      val currentUser =
          userState.value ?: throw IllegalStateException("No authenticated user found.")

      try {
        // Create a new organization with the current user as the only admin and member
        val newOrganization =
            Organization(name = name, admins = listOf(currentUser), members = listOf(currentUser))
        // Add the new organization to the repository
        organizationRepository.insertOrganization(newOrganization, currentUser)

        // Load organizations again to ensure consistency
        loadOrganizations()
      } catch (e: Exception) {
        // Update the UI state with the error message
        _uiState.update { it.copy(errorMsg = "Failed to add organization: ${e.localizedMessage}") }
      }
    }
  }
}
