package com.android.sample.ui.profile

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.github.se.bootcamp.model.authentication.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUIState(
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)

class ProfileViewModel(
    application: Application,
    private val repository: AuthRepository = AuthRepositoryProvider.repository,
    private val preferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  private var cachedUser: User? = null

  init {
    viewModelScope.launch { loadCurrentUser() }
  }

  private fun loadCurrentUser() {
    val currentUser = repository.getCurrentUser()
    currentUser?.let { user ->
      cachedUser = user
      val displayNameOverride = readOverride(KEY_DISPLAY_NAME)
      val emailOverride = readOverride(KEY_EMAIL)
      val phoneOverride = readOverride(KEY_PHONE)

      _uiState.update {
        it.copy(
            displayName = displayNameOverride ?: user.displayName.orEmpty(),
            email = emailOverride ?: user.email.orEmpty(),
            phoneNumber = phoneOverride ?: user.phoneNumber.orEmpty())
      }
    }
        ?: run {
          cachedUser = null
          _uiState.update { ProfileUIState() }
        }
  }

  fun updateDisplayName(displayName: String) {
    _uiState.update { current -> current.copy(displayName = displayName) }
  }

  fun updateEmail(email: String) {
    _uiState.update { current -> current.copy(email = email) }
  }

  fun updatePhoneNumber(phoneNumber: String) {
    _uiState.update { current -> current.copy(phoneNumber = phoneNumber) }
  }

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

  // Updated isNotBlank() -> isNotEmpty() for consistency with saveProfile
  private fun persistOverride(key: String, newValue: String, baseValue: String?) {
    val valueToStore = newValue.takeIf { it.isNotEmpty() && it != baseValue.orEmpty() }
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

    fun provideFactory(
        application: Application,
        repository: AuthRepository = AuthRepositoryProvider.repository
    ): ViewModelProvider.Factory {
      return object : ViewModelProvider.AndroidViewModelFactory(application) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ProfileViewModel(application, repository) as T
          }
          return super.create(modelClass)
        }

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
          if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val app = extras[APPLICATION_KEY] ?: application
            @Suppress("UNCHECKED_CAST") return ProfileViewModel(app, repository) as T
          }
          return super.create(modelClass, extras)
        }
      }
    }
  }
}
