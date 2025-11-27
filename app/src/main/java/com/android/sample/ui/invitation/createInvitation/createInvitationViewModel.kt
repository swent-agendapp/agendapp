package com.android.sample.ui.invitation.createInvitation

import androidx.lifecycle.ViewModel
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.OrganizationRepository
import com.android.sample.model.organization.OrganizationRepositoryProvider
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val MIN_INVITATION_COUNT = 1
const val MAX_INVITATION_COUNT = 99
const val MAX_INVITATION_COUNT_ERROR_MSG =
    "Cannot create more than $MAX_INVITATION_COUNT invitations at once."
const val MIN_INVITATION_COUNT_ERROR_MSG =
    "Cannot create less than $MIN_INVITATION_COUNT invitations."
const val INVALID_INVITATION_COUNT_ERROR_MSG =
    "Invitation count must be between $MIN_INVITATION_COUNT and $MAX_INVITATION_COUNT."
/**
 * UI state representing the current draft for invitation creation.
 *
 * @property count The number of invitations the user intends to create. Defaults to 0.
 * @property errorMsg Optional error message (e.g., invalid input).
 */
data class CreateInvitationUIState(val count: Int = MIN_INVITATION_COUNT, val errorMsg: String? = null)

/**
 * ViewModel that manages a simple state: how many invitations the user wants to create.
 *
 * Responsibilities:
 * - Holds the current count
 * - Exposes increment / decrement / setCount operations
 * - Ensures the count never goes below zero
 */
class CreateInvitationViewModel(
    private val invitationRepository: InvitationRepository =
        InvitationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {

  private val _uiState = MutableStateFlow(CreateInvitationUIState())

  /** Public immutable state observed by the UI. */
  val uiState: StateFlow<CreateInvitationUIState> = _uiState.asStateFlow()

  /**
   * Creates multiple invitations and inserts them into the repository.
   *
   * This method:
   * - Retrieves the currently authenticated user (required to assign creator/issuer info).
   * - Creates `count` new invitations using `Invitation.create(...)`.
   * - Persists each invitation through the `invitationRepository`.
   *
   * @param count The number of invitations to generate and store.
   * @throws IllegalStateException if no authenticated user is found.
   *
   * This function is typically called after the user confirms the creation from the invitation
   * creation bottom sheet.
   */
  suspend fun addInvitations() {
    val user =
        authRepository.getCurrentUser()
            ?: throw IllegalStateException("No authenticated user found.")
    val selectedOrganizationId =
        selectedOrganizationViewModel.selectedOrganizationId.value
            ?: throw IllegalStateException("No organization selected.")
    val selectedOrganization =
        organizationRepository.getOrganizationById(selectedOrganizationId, user)
            ?: throw IllegalStateException("Selected organization not found.")
    repeat(_uiState.value.count) {
      val invitation = Invitation.create(selectedOrganization)
      invitationRepository.insertInvitation(item = invitation, user = user)
    }
  }

  /** Returns true if the current state allows creating invitations. */
  fun canCreateInvitations() =
      _uiState.value.errorMsg == null &&
          _uiState.value.count >= MIN_INVITATION_COUNT &&
          _uiState.value.count <= MAX_INVITATION_COUNT

  /** Increments the invitation count by 1, if not greater than [MAX_INVITATION_COUNT] */
  fun increment() {
    val current = _uiState.value.count
    if (current < MAX_INVITATION_COUNT) {
      _uiState.value = _uiState.value.copy(count = current + 1, errorMsg = null)
    } else {
      _uiState.value = _uiState.value.copy(errorMsg = MAX_INVITATION_COUNT_ERROR_MSG)
    }
  }

  /** Decrements the invitation count by 1, staying at 0 minimum. */
  fun decrement() {
    val current = _uiState.value.count
    if (current > MIN_INVITATION_COUNT) {
      _uiState.value = _uiState.value.copy(count = current - 1, errorMsg = null)
    } else {
      _uiState.value = _uiState.value.copy(errorMsg = MIN_INVITATION_COUNT_ERROR_MSG)
    }
  }

  /**
   * Sets the number of invitations.
   *
   * If the provided value is negative or greater than [MAX_INVITATION_COUNT], the count is not
   * updated and an error message is shown instead.
   */
  fun setCount(newValue: Int) {
    if (newValue <= MIN_INVITATION_COUNT || newValue > MAX_INVITATION_COUNT) {
      _uiState.value = _uiState.value.copy(errorMsg = INVALID_INVITATION_COUNT_ERROR_MSG)
    } else {
      _uiState.value = _uiState.value.copy(count = newValue, errorMsg = null)
    }
  }
}
