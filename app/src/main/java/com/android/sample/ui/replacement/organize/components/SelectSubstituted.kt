package com.android.sample.ui.replacement.organize.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.replacement.organize.ReplacementOrganizeViewModel
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightExtraHeavy

// Assisted by AI

/**
 * Screen allowing the admin to choose **which member needs to be replaced**.
 *
 * This screen is part of the Organize Replacement workflow. It displays:
 * - A search bar for filtering members
 * - A scrollable list of selectable members
 * - A read-only field showing the currently selected member
 * - Navigation buttons to continue the workflow
 *
 * The parent `ReplacementOrganizeScreen` owns the navigation logic. This composable does **not**
 * perform navigation itself — instead it exposes callbacks that are invoked based on user actions.
 *
 * ### UX behavior
 * - Tapping a member highlights it and triggers `onMemberSelected()`.
 * - Pressing **Select events** triggers `onSelectEvents()`.
 * - Pressing **Select a date range** triggers `onSelectDateRange()`.
 * - The top bar back button triggers `onBack()`.
 *
 * @param onSelectEvents Called when the user confirms the member and wants to choose affected
 *   events.
 * @param onSelectDateRange Called when the user chooses to specify a time range first.
 * @param onBack Called when the user returns to the previous step (top bar back action).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSubstitutedScreen(
    onSelectEvents: () -> Unit = {},
    onSelectDateRange: () -> Unit = {},
    onBack: () -> Unit = {},
    replacementOrganizeViewModel: ReplacementOrganizeViewModel = viewModel()
) {
  val uiState by replacementOrganizeViewModel.uiState.collectAsState()

  // Filter the list when search changes
  val filteredMembers =
      uiState.memberList.filter { member ->
        (member.displayName?.contains(uiState.memberSearchQuery, ignoreCase = true) == true) ||
            (member.email?.contains(uiState.memberSearchQuery, ignoreCase = true) == true) ||
            (member.id.contains(uiState.memberSearchQuery, ignoreCase = true))
      }
  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.organize_replacement),
            onClick = onBack,
            backButtonTestTags = ReplacementOrganizeTestTags.BACK_BUTTON)
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = PaddingExtraLarge)
                    .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {

              /** Title * */
              Text(
                  text = stringResource(R.string.which_member_to_be_replaced),
                  style = MaterialTheme.typography.headlineMedium,
                  textAlign = TextAlign.Center,
                  modifier =
                      Modifier.padding(vertical = PaddingLarge)
                          .testTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT))

              /** Scrollable selectable list * */
              Card(
                  modifier = Modifier.fillMaxWidth().weight(WeightExtraHeavy),
                  elevation = CardDefaults.cardElevation(defaultElevation = DefaultCardElevation),
                  shape = RoundedCornerShape(CornerRadiusLarge)) {
                    Column(Modifier.fillMaxSize()) {

                      /** Search bar * */
                      TextField(
                          value = uiState.memberSearchQuery,
                          onValueChange = { replacementOrganizeViewModel.setMemberSearchQuery(it) },
                          placeholder = { Text(text = stringResource(R.string.search_member)) },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(ReplacementOrganizeTestTags.SEARCH_BAR),
                          singleLine = true,
                          trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription =
                                    stringResource(R.string.search_icon_content_description))
                          },
                          shape = RoundedCornerShape(CornerRadiusLarge))

                      /** Scrollable list * */
                      LazyColumn(
                          modifier =
                              Modifier.weight(WeightExtraHeavy)
                                  .fillMaxWidth()
                                  .testTag(ReplacementOrganizeTestTags.MEMBER_LIST),
                          verticalArrangement = Arrangement.Top) {
                            items(filteredMembers) { member ->
                              val isSelected = member == uiState.selectedMember

                              Box(
                                  modifier =
                                      Modifier.fillMaxWidth()
                                          .background(
                                              if (isSelected) GeneralPalette.Secondary
                                              else Color.White)
                                          .clickable {
                                            replacementOrganizeViewModel.setSelectedMember(member)
                                          }
                                          .padding(vertical = PaddingMedium),
                                  contentAlignment = Alignment.Center) {
                                    Text(
                                        text = member.email ?: member.id,
                                        textAlign = TextAlign.Center)
                                  }

                              HorizontalDivider(
                                  thickness = DividerDefaults.Thickness,
                                  color = DividerDefaults.color)
                            }
                          }

                      /** Read-only selected member field * */
                      OutlinedTextField(
                          value =
                              stringResource(
                                  R.string.selected_member, uiState.selectedMember?.email ?: ""),
                          onValueChange = {}, // ignored because readOnly
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(ReplacementOrganizeTestTags.SELECTED_MEMBER_INFO),
                          singleLine = true,
                          shape = RoundedCornerShape(CornerRadiusLarge),
                          colors =
                              OutlinedTextFieldDefaults.colors(
                                  focusedBorderColor = Color.Transparent,
                                  unfocusedBorderColor = Color.Transparent,
                                  disabledBorderColor = Color.Transparent,
                              ),
                          readOnly = true)
                    }
                  }

              /** Buttons * */
              Column(
                  modifier = Modifier.fillMaxWidth().padding(vertical = PaddingLarge),
                  verticalArrangement = Arrangement.spacedBy(PaddingMedium),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    SecondaryButton(
                        Modifier.testTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON),
                        text = stringResource(R.string.select_events),
                        enabled = uiState.selectedMember != null,
                        onClick = onSelectEvents)
                    SecondaryButton(
                        Modifier.testTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON),
                        text = stringResource(R.string.select_date_range),
                        enabled = uiState.selectedMember != null,
                        onClick = onSelectDateRange)
                  }
            }
      },
  )
}

@Preview
@Composable
fun SelectSubstitutedScreenPreview() {
  SelectSubstitutedScreen()
}
