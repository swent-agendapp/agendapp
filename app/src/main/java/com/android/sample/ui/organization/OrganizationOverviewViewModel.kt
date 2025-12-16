package com.android.sample.ui.organization

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.R
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
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
    val memberList: List<User> = emptyList(),
    val adminList: List<User> = emptyList(),
    val isAdmin: Boolean = false,
    @StringRes val errorMessageId: Int? = null
)

/** ViewModel for managing the state and logic of the Organization Overview screen. */
class OrganizationOverviewViewModel(
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
) : ViewModel() {
  // State holding the UI state for organization overview
  private val _uiState = MutableStateFlow(OrganizationOverviewUIState())
  val uiState: StateFlow<OrganizationOverviewUIState> = _uiState

  // Current authenticated user
  private val currentUser = authRepository.getCurrentUser()

  /** Sets an error message in the UI state using a string resource ID. */
  fun setError(@StringRes resId: Int) {
    _uiState.value = _uiState.value.copy(errorMessageId = resId)
  }
  /** Clears any error message from the UI state. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessageId = null)
  }

  /** Fills the UI state with details of the selected organization by its ID. */
  fun fillSelectedOrganizationDetails(orgId: String) {
    // Ensure the current user is not null
    if (currentUser == null) {
      setError(R.string.error_no_authenticated_user)
      return
    }

    // Ensure an organization ID is provided
    if (orgId.isEmpty()) {
      setError(R.string.error_no_organization_selected)
      return
    }

    viewModelScope.launch {
      // Fetch organization details from the repository
      val org =
          organizationRepository.getOrganizationById(organizationId = orgId, user = currentUser)
      if (org == null) {
        setError(R.string.error_organization_not_found)
        return@launch
      }
      // Fetch members of the organization
      val members =
          userRepository.getUsersByIds(userRepository.getMembersIds(organizationId = orgId))
      val admins = userRepository.getUsersByIds(userRepository.getAdminsIds(organizationId = orgId))

      // Check if the current user is an admin of the organization
      val isAdmin = userRepository.getAdminsIds(organizationId = orgId).contains(currentUser.id)

      // Update the UI state with organization details
      setMemberList(members)
      setAdminList(admins)
      setOrganizationName(org.name)
      setIsAdmin(isAdmin)
    }
  }

  /** Updates the organization name in the UI state. */
  fun setOrganizationName(name: String) {
    _uiState.value = _uiState.value.copy(organizationName = name)
  }

  /** Updates the member list in the UI state. */
  fun setMemberList(members: List<User>) {
    _uiState.value = _uiState.value.copy(memberList = members)
  }

  /** Updates the admin list in the UI state. */
  fun setAdminList(admins: List<User>) {
    _uiState.value = _uiState.value.copy(adminList = admins)
  }

  /** Sets whether the current user is an admin of the selected organization. */
  fun setIsAdmin(isAdmin: Boolean) {
    _uiState.value = _uiState.value.copy(isAdmin = isAdmin)
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
      setError(R.string.error_no_organization_to_delete)
      return
    }

    // Ensure the current user is not null
    if (currentUser == null) {
      setError(R.string.error_no_authenticated_user)
      return
    }

    viewModelScope.launch {
      // Delete the organization from the repository
      organizationRepository.deleteOrganization(organizationId = orgId, user = currentUser)
      clearSelectedOrganization()
    }
  }
}
