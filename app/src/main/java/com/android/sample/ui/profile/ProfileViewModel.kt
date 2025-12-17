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
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUIState(
    val profileOwnerId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val privilege: ProfilePrivilege = ProfilePrivilege.EMPLOYEE_TO_EMPLOYEES
)

enum class ProfilePrivilege {
  ADMIN_TO_ADMINS,
  ADMIN_TO_EMPLOYEES,
  EMPLOYEE_TO_ADMINS,
  EMPLOYEE_TO_EMPLOYEES,
  OWN_PROFILE
}

class ProfileViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
    private val preferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  private val selectedOrgId = selectedOrganizationViewModel.selectedOrganizationId.value

  private var cachedUser: User? = null

  fun loadProfile(profileOwnerId: String) {
    viewModelScope.launch {
      val currentUser = authRepository.getCurrentUser()
      val profileOwner = userRepository.getUserById(profileOwnerId)

      profileOwner?.let { profileOwner ->
        cachedUser = profileOwner
        val displayNameOverride = readOverride(KEY_DISPLAY_NAME)
        val emailOverride = readOverride(KEY_EMAIL)
        val phoneOverride = readOverride(KEY_PHONE)

        val adminList =
            if (selectedOrgId != null) userRepository.getAdminsIds(selectedOrgId) else emptyList()

        val currentUserIsAdmin = adminList.contains(currentUser?.id)
        val profileOwnerIsAdmin = adminList.contains(profileOwnerId)

        _uiState.update {
          it.copy(
              profileOwnerId = profileOwner.id,
              displayName = displayNameOverride ?: profileOwner.displayName.orEmpty(),
              email = emailOverride ?: profileOwner.email.orEmpty(),
              phoneNumber = phoneOverride ?: profileOwner.phoneNumber.orEmpty(),
              privilege =
                  when {
                    currentUser == null -> ProfilePrivilege.EMPLOYEE_TO_EMPLOYEES
                    currentUser.id == profileOwner.id -> ProfilePrivilege.OWN_PROFILE
                    currentUserIsAdmin && profileOwnerIsAdmin -> ProfilePrivilege.ADMIN_TO_ADMINS
                    currentUserIsAdmin && !profileOwnerIsAdmin ->
                        ProfilePrivilege.ADMIN_TO_EMPLOYEES
                    !currentUserIsAdmin && profileOwnerIsAdmin ->
                        ProfilePrivilege.EMPLOYEE_TO_ADMINS
                    else -> ProfilePrivilege.EMPLOYEE_TO_EMPLOYEES
                  })
        }
      }
          ?: run {
            cachedUser = null
            _uiState.update { ProfileUIState() }
          }
    }
  }

  /**
   * Promotes a user to admin status within the specified organization.
   *
   * @param userId The ID of the user to promote.
   */
  fun promoteToAdmin(userId: String) {
    viewModelScope.launch {
      if (selectedOrgId == null) {
        return@launch
      }
      val orgId = selectedOrgId
      userRepository.addAdminToOrganization(organizationId = orgId, userId = userId)
    }
  }

  fun updateDisplayName(displayName: String) {
    if (_uiState.value.privilege == ProfilePrivilege.EMPLOYEE_TO_EMPLOYEES) return
    _uiState.update { current -> current.copy(displayName = displayName) }
  }

  fun updateEmail(email: String) {
    if (_uiState.value.privilege != ProfilePrivilege.OWN_PROFILE) return
    _uiState.update { current -> current.copy(email = email) }
  }

  fun updatePhoneNumber(phoneNumber: String) {
    if (_uiState.value.privilege != ProfilePrivilege.OWN_PROFILE) return
    _uiState.update { current -> current.copy(phoneNumber = phoneNumber) }
  }

  fun saveProfile() {
    if (_uiState.value.privilege == ProfilePrivilege.EMPLOYEE_TO_EMPLOYEES) return
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
        authRepository: AuthRepository = AuthRepositoryProvider.repository,
        userRepository: UserRepository = UserRepositoryProvider.repository,
    ): ViewModelProvider.Factory {
      return object : ViewModelProvider.AndroidViewModelFactory(application) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application, authRepository, userRepository) as T
          }
          return super.create(modelClass)
        }

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
          if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val app = extras[APPLICATION_KEY] ?: application
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(app, authRepository, userRepository) as T
          }
          return super.create(modelClass, extras)
        }
      }
    }
  }
}
