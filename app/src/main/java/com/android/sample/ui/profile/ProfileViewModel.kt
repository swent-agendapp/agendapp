package com.android.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentification.AuthRepositoryProvider
import com.github.se.bootcamp.model.authentication.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the profile screen.
 *
 * @property displayName User's display name.
 * @property email User's email address.
 * @property phoneNumber User's phone number.
 */
data class ProfileUIState(
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)

/**
 * ViewModel for the Profile screen.
 *
 * @property repository The repository used to retrieve user information.
 */
class ProfileViewModel(private val repository: AuthRepository = AuthRepositoryProvider.repository) :
    ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  init {
    viewModelScope.launch { loadCurrentUser() }
  }

  /** Loads the current user from the repository. */
  private suspend fun loadCurrentUser() {
    val currentUser = repository.getCurrentUser()
    currentUser?.let { user ->
      _uiState.update {
        it.copy(
            displayName = user.displayName ?: "",
            email = user.email ?: "",
            phoneNumber = user.phoneNumber ?: "")
      }
    }
  }

  /** Updates the display name in the UI state. */
  fun updateDisplayName(displayName: String) {
    _uiState.update { it.copy(displayName = displayName) }
  }

  /** Updates the email in the UI state. */
  fun updateEmail(email: String) {
    _uiState.update { it.copy(email = email) }
  }

  /** Updates the phone number in the UI state. */
  fun updatePhoneNumber(phoneNumber: String) {
    _uiState.update { it.copy(phoneNumber = phoneNumber) }
  }

  /** Saves the profile (placeholder - would update backend in real implementation). */
  fun saveProfile() {
    // Later : Implement profile saving to backend
    // This would typically call repository.updateUser(...)
  }
}
