package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.R
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val ERROR_MESSAGE_NO_AUTHENTICATED_USER = "No authenticated user found."
/**
 * UI state data class for the Organization Overview screen.
 *
 * organizationName: Name of the organization. memberCount: Number of members in the organization.
 * errorMessageId: Resource ID for any error message to display (nullable).
 */
data class OrganizationOverviewUIState(
    val organizationName: String = "",
    val memberCount: Int = 0,
    val errorMessageId: String? = null
)

/** ViewModel for managing the state and logic of the Organization Overview screen. */
class OrganizationOverviewViewModel(
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
) : ViewModel() {

  // State holding the UI state for organization overview
  private val _uiState = MutableStateFlow(OrganizationOverviewUIState())
  val uiState: StateFlow<OrganizationOverviewUIState> = _uiState

  // Current authenticated user
  private val currentUser = authRepository.getCurrentUser()

  /** Sets an error message in the UI state using a string resource ID. */
  fun setError(error: String) {
    _uiState.value = _uiState.value.copy(errorMessageId = error)
  }
  /** Clears any error message from the UI state. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessageId = null)
  }

  /** Fills the UI state with details of the selected organization by its ID. */
  fun fillSelectedOrganizationDetails(orgId: String) {
    // Ensure the current user is not null
    if (currentUser == null) {
      setError(ERROR_MESSAGE_NO_AUTHENTICATED_USER)
      return
    }

    // Ensure an organization ID is provided
    if (orgId.isEmpty()) {
      setError(ERROR_MESSAGE_NO_AUTHENTICATED_USER)
      return
    }

    viewModelScope.launch {
      // Fetch organization details from the repository
      val org =
          organizationRepository.getOrganizationById(organizationId = orgId, user = currentUser)

      // Update the UI state with organization details
      _uiState.value =
          OrganizationOverviewUIState(
              organizationName = org?.name ?: "", memberCount = org?.members?.size ?: 0)
    }
  }

  /** Clears the selected organization from the repository and resets the UI state. */
  fun clearSelectedOrganization() {
    SelectedOrganizationRepository.clearSelection()
    _uiState.value = OrganizationOverviewUIState()
  }

  /** Deletes the selected organization by its ID. */
  fun deleteSelectedOrganization(orgId: String?) {
    // Ensure an organization ID is provided
    if (orgId.isNullOrEmpty()) {
      setError(ERROR_MESSAGE_NO_AUTHENTICATED_USER)
      return
    }

    // Ensure the current user is not null
    if (currentUser == null) {
      setError(Context.getS(R.string.error_no_authenticated_user))
      return
    }

    viewModelScope.launch {
      // Delete the organization from the repository
      organizationRepository.deleteOrganization(organizationId = orgId, user = currentUser)
      clearSelectedOrganization()
    }
  }
}
