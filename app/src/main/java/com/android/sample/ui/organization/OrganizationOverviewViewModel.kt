package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OrganizationOverviewUIState(val organizationName: String = "", val memberCount: Int = 0)

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

  fun fillSelectedOrganizationDetails(orgId: String) {
    // Ensure the current user is not null
    require(currentUser != null) { "No authenticated user found." }
    require(orgId.isNotEmpty()) { "No organization selected" }

    viewModelScope.launch {
      val org =
          organizationRepository.getOrganizationById(organizationId = orgId, user = currentUser)

      // Update the UI state with organization details
      _uiState.value =
          OrganizationOverviewUIState(
              organizationName = org?.name ?: "", memberCount = org?.members?.size ?: 0)
    }
  }

  fun clearSelectedOrganization() {
    SelectedOrganizationRepository.clearSelection()
    _uiState.value = OrganizationOverviewUIState()
  }

  fun deleteSelectedOrganization(orgId: String?) {
    require(!orgId.isNullOrEmpty()) { "No organization selected to delete." }
    require(currentUser != null) { "No authenticated user found." }

    viewModelScope.launch {
      organizationRepository.deleteOrganization(organizationId = orgId, user = currentUser)
      clearSelectedOrganization()
    }
  }
}
