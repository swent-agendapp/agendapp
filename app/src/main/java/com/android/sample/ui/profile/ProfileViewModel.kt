package com.android.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.FirebaseUserRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
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
    val phoneNumber: String = "",
    val googleDisplayName: String = "",
    val googleEmail: String = "",
    val googlePhoneNumber: String = "",
)

/**
 * ViewModel for the Profile screen.
 *
 * @property repository The repository used to retrieve user information.
 */
class ProfileViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository,
    private val userRepository: UserRepository = FirebaseUserRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  private var currentUserId: String? = null

  init {
    viewModelScope.launch { loadCurrentUser() }
  }

  /** Loads the current user from the repository. */
  private suspend fun loadCurrentUser() {
    val currentUser = repository.getCurrentUser() ?: return
    currentUserId = currentUser.id

    val googleDisplayName = currentUser.googleDisplayName ?: currentUser.displayName
    val googleEmail = currentUser.googleEmail ?: currentUser.email
    val googlePhone = currentUser.googlePhoneNumber ?: currentUser.phoneNumber

    val storedUser = runCatching { userRepository.getUser(currentUser.id) }.getOrNull()

    val finalGoogleDisplayName =
        storedUser?.googleDisplayName?.takeIf { it.isNotBlank() }
            ?: googleDisplayName.orEmpty()
    val finalGoogleEmail =
        storedUser?.googleEmail?.takeIf { it.isNotBlank() } ?: googleEmail.orEmpty()
    val finalGooglePhone =
        storedUser?.googlePhoneNumber?.takeIf { it.isNotBlank() } ?: googlePhone.orEmpty()

    val displayName =
        storedUser?.displayName?.takeIf { it.isNotBlank() } ?: finalGoogleDisplayName
    val email = storedUser?.email?.takeIf { it.isNotBlank() } ?: finalGoogleEmail
    val phone = storedUser?.phoneNumber?.takeIf { it.isNotBlank() } ?: finalGooglePhone

    _uiState.update {
      it.copy(
          displayName = displayName,
          email = email,
          phoneNumber = phone,
          googleDisplayName = finalGoogleDisplayName,
          googleEmail = finalGoogleEmail,
          googlePhoneNumber = finalGooglePhone)
    }

    val needsGoogleSync =
        storedUser?.let {
          it.googleDisplayName != googleDisplayName ||
              it.googleEmail != googleEmail ||
              it.googlePhoneNumber != googlePhone
        } ?: true

    if (needsGoogleSync) {
      runCatching {
            userRepository.upsertUser(
                User(
                    id = currentUser.id,
                    displayName = storedUser?.displayName,
                    email = storedUser?.email,
                    phoneNumber = storedUser?.phoneNumber,
                    googleDisplayName = googleDisplayName,
                    googleEmail = googleEmail,
                    googlePhoneNumber = googlePhone))
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
    val userId = currentUserId ?: return
    val state = _uiState.value

    viewModelScope.launch {
      userRepository.upsertUser(
          User(
              id = userId,
              displayName = state.displayName.ifBlank { null },
              email = state.email.ifBlank { null },
              phoneNumber = state.phoneNumber.ifBlank { null },
              googleDisplayName = state.googleDisplayName.ifBlank { null },
              googleEmail = state.googleEmail.ifBlank { null },
              googlePhoneNumber = state.googlePhoneNumber.ifBlank { null }))
    }
  }
}
