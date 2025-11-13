package com.android.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.profile.FirebaseUserProfileRepository
import com.android.sample.model.profile.UserProfileData
import com.android.sample.model.profile.UserProfileRepository
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
    private val profileRepository: UserProfileRepository = FirebaseUserProfileRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  private var currentUserId: String? = null

  init {
    viewModelScope.launch { loadCurrentUser() }
  }

  /** Loads the current user from the repository. */
  private suspend fun loadCurrentUser() {
    val currentUser = repository.getCurrentUser()
    currentUser?.let { user ->
      currentUserId = user.id

      val googleDisplayName = user.googleDisplayName ?: user.displayName
      val googleEmail = user.googleEmail ?: user.email
      val googlePhone = user.googlePhoneNumber ?: user.phoneNumber

      // Persist Google-provided information so it can be referenced later.
      runCatching {
            profileRepository.upsertProfile(
                user.id,
                UserProfileData(
                    googleDisplayName = googleDisplayName,
                    googleEmail = googleEmail,
                    googlePhoneNumber = googlePhone))
          }

      val storedProfile = runCatching { profileRepository.getProfile(user.id) }.getOrNull()

      val displayName = storedProfile?.displayName?.takeIf { it.isNotBlank() } ?: user.displayName
      val email = storedProfile?.email?.takeIf { it.isNotBlank() } ?: user.email
      val phone = storedProfile?.phoneNumber?.takeIf { it.isNotBlank() } ?: user.phoneNumber

      _uiState.update {
        it.copy(
            displayName = displayName.orEmpty(),
            email = email.orEmpty(),
            phoneNumber = phone.orEmpty(),
            googleDisplayName =
                storedProfile?.googleDisplayName ?: googleDisplayName.orEmpty(),
            googleEmail = storedProfile?.googleEmail ?: googleEmail.orEmpty(),
            googlePhoneNumber = storedProfile?.googlePhoneNumber ?: googlePhone.orEmpty())
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
      profileRepository.upsertProfile(
          userId,
          UserProfileData(
              displayName = state.displayName,
              email = state.email,
              phoneNumber = state.phoneNumber))
    }
  }
}
