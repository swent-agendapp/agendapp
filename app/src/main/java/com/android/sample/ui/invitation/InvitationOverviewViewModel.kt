package com.android.sample.ui.invitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationRepository
import com.android.sample.model.organization.invitation.InvitationRepositoryProvider
import com.android.sample.model.organization.repository.OrganizationRepository
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Invitation Overview screen.
 *
 * @property invitations List of invitations currently displayed in the UI.
 * @property isLoading Whether invitation data is currently being loaded.
 * @property error Optional error message to display when a failure occurs.
 */
data class InvitationOverviewUIState(
    val invitations: List<Invitation> = emptyList(),
    val isLoading: Boolean = false,
    val showBottomSheet: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel responsible for managing the invitation overview logic.
 *
 * This ViewModel:
 * - Loads invitations belonging to a specific organization
 * - Exposes the UI state via a [StateFlow]
 * - Handles errors and loading indicators
 * - Supports deleting invitations and updating the state accordingly
 *
 * @param invitationRepository Repository used to fetch and modify invitations.
 * @param organizationRepository Repository used to fetch organization data.
 * @param authRepository Repository providing information about the authenticated user.
 */
class InvitationOverviewViewModel(
    private val invitationRepository: InvitationRepository =
        InvitationRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(InvitationOverviewUIState())
  val uiState: StateFlow<InvitationOverviewUIState> = _uiState

  /**
   * Loads the invitations associated with the given organization.
   *
   * Updates [uiState] with:
   * - `isLoading = true` while loading
   * - Filtered list of invitations on success
   * - Error message on failure
   *
   * @param organizationId ID of the organization whose invitations should be fetched.
   */
  fun loadInvitations(organizationId: String) {
    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
    viewModelScope.launch {
      try {
        val allInvitations = invitationRepository.getAllInvitations()
        val filtered = allInvitations.filter { it.organizationId == organizationId }
        _uiState.value =
            _uiState.value.copy(invitations = filtered, isLoading = false, error = null)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
      }
    }
  }

  /**
   * Deletes an invitation and updates the UI state.
   *
   * Steps:
   * - Fetches the authenticated user
   * - Retrieves the invitation and its associated organization
   * - Performs the delete operation via the repository
   * - Removes the deleted invitation from the UI state
   *
   * Updates [uiState] with an error message if deletion fails.
   *
   * @param invitationId The ID of the invitation to be deleted.
   */
  fun deleteInvitation(
      invitationId: String,
  ) {
    viewModelScope.launch {
      try {
        val user =
            authRepository.getCurrentUser()
                ?: throw IllegalStateException("No authenticated user found.")
        val invitation =
            invitationRepository.getInvitationById(invitationId)
                ?: throw IllegalStateException("Invitation with ID = \"$invitationId\" not found.")
        val organization =
            organizationRepository.getOrganizationById(invitation.organizationId, user)
                ?: throw IllegalStateException(
                    "Organization with ID = \"${invitation.organizationId}\" not found.")
        invitationRepository.deleteInvitation(invitationId, organization, user)
        _uiState.value =
            _uiState.value.copy(
                invitations = _uiState.value.invitations.filterNot { it.id == invitationId })
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(error = e.message)
      }
    }
  }
  /**
   * Updates the UI state to show the bottom sheet.
   *
   * When called, the [InvitationOverviewUIState.showBottomSheet] property is set to `true`, which
   * triggers the Compose UI to display the bottom sheet.
   */
  fun showBottomSheet() {
    _uiState.update { it.copy(showBottomSheet = true) }
  }
  /**
   * Updates the UI state to hide the bottom sheet.
   *
   * When called, the [InvitationOverviewUIState.showBottomSheet] property is set to `false`, which
   * triggers the Compose UI to dismiss the bottom sheet.
   */
  fun dismissBottomSheet() {
    _uiState.update { it.copy(showBottomSheet = false) }
  }
}
