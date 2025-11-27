package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.OrganizationRepository
import com.android.sample.model.organization.OrganizationRepositoryProvider
import com.android.sample.model.organization.SelectedOrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

  suspend fun fillSelectedOrganizationDetails(orgId: String) {
    // Ensure the current user is not null
    require(currentUser != null) { "No authenticated user found." }
    require(orgId.isNotEmpty()) { "No organization selected" }

    val org = organizationRepository.getOrganizationById(organizationId = orgId, user = currentUser)

    val orgName = org?.name ?: ""
    val members = org?.members ?: emptyList()
    val memberCount = members.size

    // Update the UI state with organization details
    _uiState.value =
        OrganizationOverviewUIState(organizationName = orgName, memberCount = memberCount)
  }

  fun clearSelectedOrganization() {
    SelectedOrganizationRepository.clearSelection()
    _uiState.value = OrganizationOverviewUIState()
  }

  suspend fun deleteSelectedOrganization(orgId: String?) {
    require(!orgId.isNullOrEmpty()) { "No organization selected to delete." }
    require(currentUser != null) { "No authenticated user found." }

    organizationRepository.deleteOrganization(organizationId = orgId, user = currentUser)
    clearSelectedOrganization()
  }
}
