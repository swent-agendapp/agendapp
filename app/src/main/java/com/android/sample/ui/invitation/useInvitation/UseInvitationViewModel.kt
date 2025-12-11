package com.android.sample.ui.invitation.useInvitation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.R
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.invitation.Invitation.Companion.INVITATION_CODE_LENGTH
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryProvider
import com.android.sample.model.organization.invitation.InvitationStatus
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state representing the current draft for invitation creation.
 *
 * @property code The code that the user has entered. Defaults to an empty string.
 * @property isTemptingToJoin Indicates whether the user is currently attempting to join an
 *   organization.
 * @property isInputCodeIllegal Indicates whether the user is tempting to enter illegal characters or surpass allowed length.
 * @property errorMessageId Optional ID of the error message to display when a failure occurs.
 */
data class UseInvitationUIState(
    val code: String = "",
    val isTemptingToJoin: Boolean = false,
    val isInputCodeIllegal: Boolean = false,
    @StringRes val errorMessageId: Int? = null,
)

class UseInvitationViewModel(
    private val invitationRepository: InvitationRepository =
        InvitationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(UseInvitationUIState())

  /** Public immutable state observed by the UI. */
  val uiState: StateFlow<UseInvitationUIState> = _uiState.asStateFlow()

  /**
   * Attempts to join an organization using the invitation code currently stored in the UI state.
   *
   * The process is executed inside a coroutine in [viewModelScope] and proceeds as follows:
   * 1. Retrieves the currently authenticated user.
   *     - If no user is authenticated, an error is emitted and the operation stops.
   * 2. Retrieves the invitation associated with the entered code.
   *     - If no invitation matches the code, an error is emitted.
   * 3. Validates the invitation's status:
   *     - If the invitation is already used, an error is shown.
   *     - If the invitation is expired, an error is shown.
   * 4. If the invitation is valid, attempts to add the authenticated user as a member of the
   *    organization referenced by the invitation.
   *     - If this operation fails, a generic joining error is emitted.
   *
   * All errors are communicated to the UI through [setError], with optional formatting arguments.
   */
  fun joinWithCode() {
    setIsTemptingToJoin(true)
    viewModelScope.launch {

      // Step 1: Get the currently authenticated user.
      val user = authRepository.getCurrentUser()
      if (user == null) {
        setError(R.string.error_no_authenticated_user)
        setIsTemptingToJoin(false)
        return@launch
      }

      // Step 2: Retrieve the invitation by code.
      val invitation = invitationRepository.getInvitationByCode(_uiState.value.code)
      if (invitation == null) {
        setError(R.string.error_invitation_with_code_not_found)
        setIsTemptingToJoin(false)
        return@launch
      }

      // Step 3: Validate the invitation status.
      if (invitation.status == InvitationStatus.Used) {
        setError(R.string.error_invitation_already_used)
        setIsTemptingToJoin(false)
        return@launch
      }
      if (invitation.status == InvitationStatus.Expired) {
        setError(R.string.error_invitation_expired)
        setIsTemptingToJoin(false)
        return@launch
      }

      // Step 4: Attempt to add the user to the organization.
      val success =
          try {
            organizationRepository.addMemberToOrganization(user, invitation)
            true
          } catch (_: Exception) {
            setError(R.string.error_joining_organization)
            false
          }

      // Update the invitation as used if joining was successful.
      if (success) {
        val organization =
            organizationRepository.getOrganizationById(invitation.organizationId, user)
        if (organization == null) {
          setError(R.string.error_organization_not_found)
          setIsTemptingToJoin(false)
          return@launch
        }
        invitationRepository.updateInvitation(
            itemId = invitation.id,
            item =
                invitation.copy(
                    inviteeEmail = user.email,
                    acceptedAt = java.time.Instant.now(),
                    status = InvitationStatus.Used,
                ),
            organization = organization,
            user = user)
        setError(null)
      }
      setIsTemptingToJoin(false)
    }
  }

  /**
   * Updates the invitation code stored in the UI state after sanitizing the user input.
   *
   * Sanitization rules:
   * - Converts all characters to uppercase.
   * - Keeps only alphanumeric characters.
   * - Truncates the result to a maximum length of [INVITATION_CODE_LENGTH].
   *
   * This ensures that the invitation code is always normalized and valid for processing, regardless
   * of how the user enters the text in the UI.
   *
   * @param rawInput The raw text entered by the user in the invitation code field.
   */
  fun setCode(rawInput: String) {
      val upper = rawInput.uppercase()
      val onlyAllowed = upper.filter { it.isLetterOrDigit() }
      val sanitized = onlyAllowed.take(INVITATION_CODE_LENGTH)

      _uiState.update { it.copy(code = sanitized) }
  }

  /**
   * Updates the UI state's error message.
   *
   * @param resId The string ID of the error message to display, or `null` to clear the current
   *   error.
   */
  fun setError(resId: Int?) {
    _uiState.update { it.copy(errorMessageId = resId) }
  }

  /**
   * Updates whether the user is currently attempting to join an organization.
   *
   * @param isTempting `true` if the user is in the process of joining, `false` otherwise.
   */
  fun setIsTemptingToJoin(isTempting: Boolean) {
    _uiState.update { it.copy(isTemptingToJoin = isTempting) }
  }

    /**
     * Updates whether the input code is illegal.
     *
     * @param isIllegal `true` if the input code contains illegal characters or surpasses allowed length, `false` otherwise.
     */
    fun setIsInputCodeIllegal(isIllegal: Boolean) {
        _uiState.update { it.copy(isInputCodeIllegal = isIllegal) }
    }
}
