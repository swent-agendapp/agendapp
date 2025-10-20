package com.android.sample.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.sample.model.authentification.User
import com.github.se.bootcamp.model.authentication.AuthRepository
import com.github.se.bootcamp.model.authentication.AuthRepositoryFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents the UI state for the profile screen.
 *
 * @property user The currently signed-in user, or null if not signed in.
 * @property showAdminContact Whether to show admin contact information.
 */
data class ProfileUIState(val user: User? = null, val showAdminContact: Boolean = false)

/**
 * ViewModel for the Profile screen.
 *
 * @property repository The repository used to retrieve user information.
 */
class ProfileViewModel(private val repository: AuthRepository = AuthRepositoryFirebase()) :
    ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  init {
    loadCurrentUser()
  }

  /** Loads the current user from the repository. */
  private fun loadCurrentUser() {
    val currentUser = repository.getCurrentUser()
    _uiState.update { it.copy(user = currentUser) }
  }

  /** Toggles the visibility of admin contact information. */
  fun toggleAdminContact() {
    _uiState.update { it.copy(showAdminContact = !it.showAdminContact) }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { ProfileViewModel() }
    }
  }
}
