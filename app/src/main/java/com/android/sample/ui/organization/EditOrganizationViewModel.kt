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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditOrganizationUIState(val name: String = "", @StringRes val errorMsgID: Int? = null)

open class EditOrganizationViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  // State holding the UI state for editing an organization
  private val _uiState = MutableStateFlow(EditOrganizationUIState())
  open val uiState: StateFlow<EditOrganizationUIState> = _uiState

  // State holding the current user
  private val _userState = MutableStateFlow(authRepository.getCurrentUser())
  val userState: StateFlow<User?> = _userState

  fun loadOrganizationData() {
    viewModelScope.launch {
      try {
        val currentUser = userState.value ?: throw IllegalStateException()

        val selectedOrgId = selectedOrganizationViewModel.getSelectedOrganizationId()
        val organization = organizationRepository.getOrganizationById(selectedOrgId, currentUser)
        if (organization == null) {
          _uiState.update { it.copy(errorMsgID = R.string.error_organization_not_found) }
          return@launch
        }

        updateName(organization.name)
      } catch (_: Exception) {
        _uiState.update { it.copy(errorMsgID = R.string.error_loading_organiation) }
      }
    }
  }

  // Update the name field in the UI state
  fun updateName(name: String) {
    _uiState.value = _uiState.value.copy(name = name)
  }

  // Validate if the organization name is not null or blank
  fun isValidOrganizationName(): Boolean {
    val name = _uiState.value.name
    return !name.isBlank()
  }

  // Add a new organization with the given name for the current user (himself as the only admin and
  // member)
  fun editOrganization() {
    viewModelScope.launch {
      try {
        val currentUser = userState.value ?: throw IllegalStateException()

        val oldOrgId = selectedOrganizationViewModel.getSelectedOrganizationId()

        val oldOrg = organizationRepository.getOrganizationById(oldOrgId, currentUser)
        if (oldOrg == null) {
          _uiState.update { it.copy(errorMsgID = R.string.error_organization_not_found) }
          return@launch
        }

        val updatedOrg = oldOrg.copy(name = _uiState.value.name)
        organizationRepository.updateOrganization(oldOrgId, updatedOrg, currentUser)
      } catch (_: Exception) {
        _uiState.update { it.copy(errorMsgID = R.string.error_editing_organization) }
      }
    }
  }
}
