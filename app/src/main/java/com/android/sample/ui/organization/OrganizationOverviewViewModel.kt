package com.android.sample.ui.organization

import androidx.annotation.StringRes
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

data class OrganizationOverviewUIState(
    val organizationName: String = "",
    val memberCount: Int = 0,
    @StringRes val errorMessageId: Int? = null
)

class OrganizationOverviewViewModel(
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
) : ViewModel() {
  // State holding the UI state for organization overview
  private val _uiState = MutableStateFlow(OrganizationOverviewUIState())
  val uiState: StateFlow<OrganizationOverviewUIState> = _uiState

  fun setError(@StringRes resId: Int) {
    _uiState.value = _uiState.value.copy(errorMessageId = resId)
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessageId = null)
  }

  // Current authenticated user
  private val currentUser = authRepository.getCurrentUser()

  fun fillSelectedOrganizationDetails(orgId: String) {
    // Ensure the current user is not null
    if (currentUser == null) {
      setError(R.string.error_no_authenticated_user)
      return
    }

    if (orgId.isEmpty()) {
      setError(R.string.error_no_organization_selected)
      return
    }

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
    if (orgId.isNullOrEmpty()) {
      setError(R.string.error_no_organization_to_delete)
      return
    }

    if (currentUser == null) {
      setError(R.string.error_no_authenticated_user)
      return
    }

    viewModelScope.launch {
      organizationRepository.deleteOrganization(organizationId = orgId, user = currentUser)
      clearSelectedOrganization()
    }
  }
}
