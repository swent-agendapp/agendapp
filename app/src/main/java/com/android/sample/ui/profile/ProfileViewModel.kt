package com.android.sample.ui.profile

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
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
class ProfileViewModel(
    application: Application,
    private val repository: AuthRepository = AuthRepositoryProvider.repository,
    private val preferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
) :
    AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  private var cachedUser: User? = null

  init {
    viewModelScope.launch { loadCurrentUser() }
  }

  /** Loads the current user from the repository. */
  private suspend fun loadCurrentUser() {
    val currentUser = repository.getCurrentUser()
    currentUser?.let { user ->
      cachedUser = user
    } ?: run { cachedUser = null }

    val displayNameOverride = readOverride(KEY_DISPLAY_NAME)
    val emailOverride = readOverride(KEY_EMAIL)
    val phoneOverride = readOverride(KEY_PHONE)

    _uiState.update {
      it.copy(
          displayName =
              displayNameOverride ?: cachedUser?.displayName.orEmpty(),
          email = emailOverride ?: cachedUser?.email.orEmpty(),
          phoneNumber = phoneOverride ?: cachedUser?.phoneNumber.orEmpty())
    }
  }

  /** Updates the display name in the UI state. */
  fun updateDisplayName(displayName: String) {
    _uiState.update { current -> current.copy(displayName = displayName) }
  }

  /** Updates the email in the UI state. */
  fun updateEmail(email: String) {
    _uiState.update { current -> current.copy(email = email) }
  }

  /** Updates the phone number in the UI state. */
  fun updatePhoneNumber(phoneNumber: String) {
    _uiState.update { current -> current.copy(phoneNumber = phoneNumber) }
  }

  /** Saves the profile (placeholder - would update backend in real implementation). */
  fun saveProfile() {
    val currentState = uiState.value
    val trimmedDisplayName = currentState.displayName.trim()
    val trimmedEmail = currentState.email.trim()
    val trimmedPhone = currentState.phoneNumber.trim()

    val resolvedDisplayName =
        trimmedDisplayName.takeIf { it.isNotEmpty() } ?: cachedUser?.displayName.orEmpty()
    val resolvedEmail = trimmedEmail.takeIf { it.isNotEmpty() } ?: cachedUser?.email.orEmpty()
    val resolvedPhoneNumber =
        trimmedPhone.takeIf { it.isNotEmpty() } ?: cachedUser?.phoneNumber.orEmpty()

    persistOverride(KEY_DISPLAY_NAME, trimmedDisplayName, cachedUser?.displayName)
    persistOverride(KEY_EMAIL, trimmedEmail, cachedUser?.email)
    persistOverride(KEY_PHONE, trimmedPhone, cachedUser?.phoneNumber)

    _uiState.update {
      it.copy(
          displayName = resolvedDisplayName,
          email = resolvedEmail,
          phoneNumber = resolvedPhoneNumber)
    }
  }

  private fun persistOverride(key: String, newValue: String, baseValue: String?) {
    val valueToStore = newValue.takeIf { it.isNotBlank() && it != baseValue.orEmpty() }
    preferences.edit {
      if (valueToStore == null) {
        remove(scopedKey(key))
      } else {
        putString(scopedKey(key), valueToStore)
      }
    }
  }

  private fun readOverride(key: String): String? {
    return preferences.getString(scopedKey(key), null)
  }

  private fun scopedKey(key: String): String {
    val userId = cachedUser?.id ?: DEFAULT_USER_KEY
    return "${userId}_${key}"
  }

  companion object {
    internal const val PREFS_NAME = "profile_prefs"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone"
    private const val DEFAULT_USER_KEY = "default_user"
  }
}
