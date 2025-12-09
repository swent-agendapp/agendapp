package com.android.sample.ui.invitation.useInvitation

import androidx.lifecycle.ViewModel
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryProvider
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI state representing the current draft for invitation creation.
 *
 * @property code The code that the user has entered. Defaults to an empty string.
 * @property errorMsg Optional error message (e.g., invalid input).
 */
data class UseInvitationUIState(val code: String = "", val errorMsg: String? = null)

class CreateInvitationViewModel(
    private val invitationRepository: InvitationRepository =
        InvitationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(UseInvitationUIState())

  /** Public immutable state observed by the UI. */
  val uiState: StateFlow<UseInvitationUIState> = _uiState.asStateFlow()

  fun joinWithCode() {
    // TODO: Implement joining logic here
  }

  fun setCode(newCode: String) {
    _uiState.update { it.copy(code = newCode) }
  }

  fun setErrorMsg(errorMsg: String?) {
    _uiState.update { it.copy(errorMsg = errorMsg) }
  }
}
