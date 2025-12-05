package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrganizationUIState(
    val isLoading: Boolean = true,
    val organizations: List<Organization> = emptyList(),
    val errorMsg: String? = null,
    val isRefreshing: Boolean = false
)

// ViewModel for managing organization data for the current user
open class OrganizationViewModel(
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
) : ViewModel() {

  // State holding the UI state of the organizations of the current user
  private val _uiState = MutableStateFlow(OrganizationUIState())
  open val uiState: StateFlow<OrganizationUIState> = _uiState

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

  // Refresh organizations for pull-to-refresh functionality
  fun refreshOrganizations() {
    viewModelScope.launch {
      try {
        // Get the current authenticated user
        val user = userState.value ?: throw IllegalStateException("No authenticated user found.")

        // Update UI state to refreshing
        _uiState.update { it.copy(isRefreshing = true) }

        // Fetch fresh organizations from repository
        val freshOrganizations = organizationRepository.getAllOrganizations(user)
        _uiState.update { it.copy(organizations = freshOrganizations, isRefreshing = false) }
      } catch (e: Exception) {
        // Update the UI state with the error message and stop refreshing
        _uiState.update {
          it.copy(errorMsg = "Failed to refresh organizations: ${e.localizedMessage}", isRefreshing = false)
        }
      }
    }
  }

  // Clear any error message in the UI state
  open fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
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
