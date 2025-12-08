package com.android.sample.ui.invitation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.R
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
    @StringRes val errorMessageId: Int? = null
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
    setLoading(true)
    viewModelScope.launch {
      try {
        val invitations = invitationRepository.getInvitationByOrganization(organizationId)
        setInvitations(invitations)
        setLoading(false)
      } catch (_: Exception) {
        setError(R.string.error_loading_invitations)
        setLoading(false)
      }
    }
  }

  /**
   * Creates multiple invitations and inserts them into the repository.
   *
   * This method:
   * - Retrieves the currently authenticated user (required to assign creator/issuer info).
   * - Creates `count` new invitations using `Invitation.create(...)`.
   * - Persists each invitation through the `invitationRepository`.
   *
   * @param count The number of invitations to generate and store.
   * @param organizationId ID of the organization for which invitation should be added.
   *
   * This function is typically called after the user confirms the creation from the invitation
   * creation bottom sheet.
   */
  fun addInvitations(organizationId: String, count: Int) {
    setLoading(true)
    viewModelScope.launch {
      val user = authRepository.getCurrentUser()
      if (user == null) {
        setError(R.string.error_no_authenticated_user)
        setLoading(false)
        return@launch
      }
      val organization = organizationRepository.getOrganizationById(organizationId, user)
      if (organization == null) {
        setError(R.string.error_organization_not_found)
        setLoading(false)
        return@launch
      }
      val success =
          try {
            repeat(count) {
              invitationRepository.insertInvitation(organization = organization, user = user)
            }
            loadInvitations(organizationId)
            true
          } catch (_: Exception) {
            setError(R.string.error_inserting_invitation)
            false
          }

      if (!success) {
        setError(R.string.error_inserting_invitation)
        setLoading(false)
        return@launch
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
  fun deleteInvitation(invitationId: String) {
    viewModelScope.launch {
      val user = authRepository.getCurrentUser()
      if (user == null) {
        setError(R.string.error_no_authenticated_user)
        return@launch
      }
      val invitation = invitationRepository.getInvitationById(invitationId)
      if (invitation == null) {
        setError(R.string.error_invitation_not_found)
        return@launch
      }
      val organization = organizationRepository.getOrganizationById(invitation.organizationId, user)
      if (organization == null) {
        setError(R.string.error_organization_not_found)
        return@launch
      }
      val success =
          try {
            invitationRepository.deleteInvitation(invitationId, organization, user)
            true
          } catch (_: Exception) {
            setError(R.string.error_invitation_not_found)
            false
          }

      if (!success) {
        setError(R.string.error_deleting_invitations)
        return@launch
      }

      // Update UI list on success
      setInvitations(_uiState.value.invitations.filterNot { it.id == invitationId })
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

  /**
   * Replaces the current list of invitations in the UI state.
   *
   * Call this to manually update the invitations displayed in the UI, for example after creating,
   * editing, or reloading data.
   *
   * @param invitations The new list of invitations to display.
   */
  fun setInvitations(invitations: List<Invitation>) {
    _uiState.update { it.copy(invitations = invitations) }
  }

  /**
   * Updates the loading state flag.
   *
   * Call this to explicitly set whether the UI should display a loading indicator.
   *
   * @param isLoading `true` to show loading state, `false` to hide it.
   */
  fun setLoading(isLoading: Boolean) {
    _uiState.update { it.copy(isLoading = isLoading) }
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
}
