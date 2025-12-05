package com.android.sample.ui.authentication

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.R
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for authentication.
 *
 * @property isLoading Whether an authentication operation is in progress.
 * @property user The currently signed-in [User], or null if not signed in.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property signedOut True if a sign-out operation has completed.
 */
data class AuthUIState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMsg: String? = null,
    val signedOut: Boolean = true,
)

/**
 * ViewModel for the Sign-In view.
 *
 * @property authRepository The repository used to perform authentication operations.
 */
class SignInViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(AuthUIState())
  val uiState: StateFlow<AuthUIState> = _uiState

  init {
    checkCurrentUser()
  }

  /** Checks if there's a persisted user session and restores it. */
  private fun checkCurrentUser() {
    val user = authRepository.getCurrentUser()

    if (user == null) {
      _uiState.update { it.copy(signedOut = true, isLoading = false, user = null) }
      return
    }

    _uiState.update { it.copy(user = user, signedOut = false, isLoading = false) }
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  private fun getSignInOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(R.string.default_web_client_id))
          .build()

  private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

  /** Initiates the Google sign-in flow and updates the UI state on success or failure. */
  fun signIn(context: Context, credentialManager: CredentialManager) {
    if (_uiState.value.isLoading) return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMsg = null) }

      val signInOptions = getSignInOptions(context)
      val signInRequest = signInRequest(signInOptions)

      try {
        // Launch Credential Manager UI safely
        val credential = getCredential(context, signInRequest, credentialManager)

        // Pass the credential to your repository
        authRepository
            .signInWithGoogle(credential)
            .fold(
                { user ->
                  userRepository.newUser(user)
                  _uiState.update { it.copy(isLoading = false, user = user, signedOut = false) }
                },
                { failure ->
                  _uiState.update {
                    it.copy(isLoading = false, errorMsg = failure.message, signedOut = true)
                  }
                })
      } catch (e: GetCredentialCancellationException) {
        // User cancelled the sign-in flow
        _uiState.update {
          it.copy(isLoading = false, errorMsg = e.localizedMessage, signedOut = true, user = null)
        }
      } catch (e: GetCredentialException) {
        // Other credential errors
        val errorMessage = e.localizedMessage.orEmpty()
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = context.getString(R.string.sign_in_credentials_error, errorMessage),
              signedOut = true,
              user = null)
        }
      } catch (e: Exception) {
        // Unexpected errors
        val errorMessage = e.localizedMessage.orEmpty()
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = context.getString(R.string.sign_in_unexpected_error, errorMessage),
              signedOut = true,
              user = null)
        }
      }
    }
  }

  /** Initiates sign-out and updates the UI state on success or failure. */
  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      authRepository
          .signOut()
          .fold(
              onSuccess = {
                _uiState.update {
                  it.copy(isLoading = false, errorMsg = null, signedOut = true, user = null)
                }
              },
              onFailure = { throwable ->
                _uiState.update {
                  it.copy(
                      isLoading = false,
                      errorMsg = throwable.localizedMessage,
                      signedOut = false,
                      user = it.user)
                }
              })
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
