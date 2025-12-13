package com.android.sample.ui.organization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddOrganizationUIState(val name: String? = null, val errorMsg: String? = null)

open class AddOrganizationViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
) : ViewModel() {
  // State holding the UI state for adding an organization
  private val _uiState = MutableStateFlow(AddOrganizationUIState())
  open val uiState: StateFlow<AddOrganizationUIState> = _uiState

  // State holding the current user
  private val _userState = MutableStateFlow(authRepository.getCurrentUser())
  val userState: StateFlow<User?> = _userState

  // Update the name field in the UI state
  open fun updateName(name: String) {
    _uiState.value = _uiState.value.copy(name = name)
  }

  // Validate if the organization name is not null or blank
  open fun isValidOrganizationName(): Boolean {
    val name = _uiState.value.name
    return !name.isNullOrBlank()
  }

  // Add a new organization with the given name for the current user (himself as the only admin and
  // member)
  fun addOrganizationFromName(name: String) {
    viewModelScope.launch {
      val currentUser = userState.value ?: throw IllegalStateException()

      try {
        // Create a new organization with the current user as the only admin and member
        val newOrganization = Organization(name = name)
        SelectedOrganizationRepository.changeSelectedOrganization(newOrganization.id)
        // Add the new organization to the repository
        organizationRepository.insertOrganization(newOrganization)
        userRepository.addAdminToOrganization(currentUser.id, newOrganization.id)
      } catch (e: Exception) {
        SelectedOrganizationRepository.clearSelection()
        // Update the UI state with the error message
        _uiState.update { it.copy(errorMsg = "Failed to add organization: ${e.localizedMessage}") }
      }
    }
  }
}
