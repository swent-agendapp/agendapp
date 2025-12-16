package com.android.sample.ui.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.ui.common.MemberList
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.CornerRadiusExtraLarge
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SizeHuge
import com.android.sample.ui.theme.SizeMassive
import com.android.sample.ui.theme.SizeMediumLarge
import com.android.sample.ui.theme.SmallCardElevation
import com.android.sample.ui.theme.SpacingExtraSmall
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium

// Test tags for OrganizationOverviewScreen composables.
object OrganizationOverviewScreenTestTags {
  const val ROOT = "organizationOverviewScreenRoot"
  const val ORGANIZATION_NAME_TEXT = "organizationNameText"
  const val ORGANIZATION_IMAGE = "organizationImage"
  const val MEMBER_COUNT_TEXT = "memberCountText"
  const val CATEGORIES_BUTTON = "categoriesButton"
  const val MEMBERS_LIST = "membersList"
  const val INVITE_MEMBERS_BUTTON = "inviteMembersButton"
  const val CHANGE_BUTTON = "changeButton"
  const val DELETE_BUTTON = "deleteButton"
  const val INVITATION_BUTTON = "invitationButton"
  const val EDIT_CATEGORY_BUTTON = "editCategoryButton"
  const val ERROR_SNACKBAR = "errorSnackBar"
}

/**
 * Screen that provides an overview of the selected organization, including details and actions.
 *
 * @param onNavigateBack Callback for navigating back to the previous screen.
 * @param onEditOrganization Callback for editing the organization details.
 * @param onCategoriesClick Callback for navigating to the categories screen.
 * @param onInvitationClick Callback for navigating to the invitation screen.
 * @param onMemberClick Callback for handling member item clicks.
 * @param onChangeOrganization Callback for changing the selected organization.
 * @param organizationOverviewViewModel ViewModel managing the organization overview state.
 * @param selectedOrganizationViewModel ViewModel managing the selected organization state.
 */
@Composable
fun OrganizationOverviewScreen(
    onNavigateBack: () -> Unit = {},
    onChangeOrganization: () -> Unit = {},
    onEditOrganization: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onInvitationClick: () -> Unit = {},
    onMemberClick: (User) -> Unit = {},
    organizationOverviewViewModel: OrganizationOverviewViewModel = viewModel(),
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel,
) {

  // Remember snackbar host state to handle error messages
  val snackBarHostState = remember { SnackbarHostState() }

  // Collect state from ViewModels
  val selectedOrgId by selectedOrganizationViewModel.selectedOrganizationId.collectAsState()
  val uiState by organizationOverviewViewModel.uiState.collectAsState()

  // Get error message string if available
  val errorMessage = uiState.errorMessageId?.let { id -> stringResource(id) }

  // Load organization details when selectedOrgId changes
  LaunchedEffect(Unit) {
    selectedOrgId?.let { organizationOverviewViewModel.fillSelectedOrganizationDetails(it) }
  }

  // Show error messages in a snack-bar
  LaunchedEffect(errorMessage) {
    errorMessage?.let { msg ->
      snackBarHostState.showSnackbar(msg)
      organizationOverviewViewModel.clearError()
    }
  }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.settings_organization_selection_button),
            onClick = onNavigateBack,
            backButtonTestTags = "")
      },
      snackbarHost = {
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.testTag(OrganizationOverviewScreenTestTags.ERROR_SNACKBAR))
      },
      modifier = Modifier.testTag(OrganizationOverviewScreenTestTags.ROOT)) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = PaddingMedium)
                    .verticalScroll(rememberScrollState())) {

              /* ----------------------------------------------------
               * 1. ORGANIZATION HEADER
               * ---------------------------------------------------- */
              Box(modifier = Modifier.fillMaxWidth().padding(vertical = PaddingMedium)) {

                // Switch organization button (top-left)
                IconButton(
                    onClick = onChangeOrganization, modifier = Modifier.align(Alignment.TopStart)) {
                      Icon(
                          imageVector = Icons.Outlined.SwapHoriz,
                          contentDescription = "Change organization")
                    }

                // Edit button (top-right)
                if (uiState.isAdmin) {
                  IconButton(
                      onClick = onEditOrganization, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit organization")
                      }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      // Organization image
                      Box(
                          modifier =
                              Modifier.size(SizeMassive)
                                  .clip(CircleShape)
                                  .background(MaterialTheme.colorScheme.surfaceVariant),
                          contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Organization image",
                                modifier =
                                    Modifier.size(SizeHuge)
                                        .testTag(
                                            OrganizationOverviewScreenTestTags.ORGANIZATION_IMAGE))
                          }

                      Spacer(modifier = Modifier.height(SpacingMedium))

                      // Organization name
                      Text(
                          text = uiState.organizationName,
                          style = MaterialTheme.typography.titleLarge,
                          fontWeight = FontWeight.Bold)

                      Spacer(modifier = Modifier.height(SpacingExtraSmall))

                      // Member count
                      Text(
                          text =
                              pluralStringResource(
                                  id = R.plurals.members_count,
                                  count = uiState.memberList.size,
                                  uiState.memberList.size),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
              }

              /* ----------------------------------------------------
               * 2. ACTION CARDS ROW
               * ---------------------------------------------------- */
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    OverviewActionCard(
                        modifier =
                            Modifier.weight(WeightExtraHeavy)
                                .testTag(OrganizationOverviewScreenTestTags.CATEGORIES_BUTTON),
                        icon = Icons.Default.Category,
                        label = stringResource(R.string.categories_button),
                        onClick = onCategoriesClick)

                    OverviewActionCard(
                        modifier =
                            Modifier.weight(WeightExtraHeavy)
                                .testTag(OrganizationOverviewScreenTestTags.INVITE_MEMBERS_BUTTON),
                        icon = Icons.Default.PersonAdd,
                        label = stringResource(R.string.invitations_button),
                        onClick = onInvitationClick)
                  }

              Spacer(modifier = Modifier.height(SpacingLarge))

              /* ----------------------------------------------------
               * 3. MEMBERS LIST
               * ---------------------------------------------------- */
              Text(
                  text = stringResource(R.string.members),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(start = PaddingMedium))
              MemberList(
                  modifier =
                      Modifier.weight(WeightExtraHeavy)
                          .testTag(OrganizationOverviewScreenTestTags.MEMBERS_LIST)
                          .padding(PaddingMedium),
                  onMemberClick = onMemberClick,
                  members = uiState.memberList,
                  admins = uiState.adminList)

              Spacer(modifier = Modifier.height(SpacingLarge))
            }
      }
}

/**
 * Composable representing an action card in the organization overview screen.
 *
 * @param modifier Modifier to be applied to the card.
 * @param icon Icon to be displayed on the card.
 * @param label Label text for the card.
 * @param onClick Callback invoked when the card is clicked.
 */
@Composable
private fun OverviewActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
  Card(
      modifier = modifier.padding(horizontal = PaddingSmall).clickable(onClick = onClick),
      shape = RoundedCornerShape(CornerRadiusExtraLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = SmallCardElevation),
      colors =
          CardColors(
              containerColor = GeneralPalette.Primary,
              contentColor = Color.White,
              disabledContainerColor = GeneralPalette.Primary.copy(alpha = WeightLight),
              disabledContentColor = Color.White.copy(alpha = WeightMedium))) {
        Column(
            modifier = Modifier.padding(vertical = PaddingSmall).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                  imageVector = icon,
                  contentDescription = label,
                  modifier = Modifier.size(SizeMediumLarge))

              Spacer(modifier = Modifier.height(SpacingSmall))

              Text(
                  text = label,
                  style = MaterialTheme.typography.bodySmall,
                  textAlign = TextAlign.Center)
            }
      }
}

@Preview
@Composable
fun OrganizationOverviewScreenPreview() {
  OrganizationOverviewScreen()
}
