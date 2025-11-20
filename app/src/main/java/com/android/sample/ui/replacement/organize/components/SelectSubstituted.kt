package com.android.sample.ui.replacement.organize.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.components.MemberSelectionList
import com.android.sample.ui.replacement.organize.ReplacementOrganizeTestTags
import com.android.sample.ui.replacement.organize.ReplacementOrganizeViewModel
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
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
    onMemberSelected: (String) -> Unit = {},
    onSelectEvents: () -> Unit = {},
    onSelectDateRange: () -> Unit = {},
    onBack: () -> Unit = {},
    replacementOrganizeViewModel: ReplacementOrganizeViewModel = viewModel()
) {
  val candidates = listOf("Emilien", "Haobin", "Noa", "Weifeng", "Timael", "Méline", "Nathan")
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
              var selectedMembers by remember { mutableStateOf(setOf<String>()) }
              Card(
                  modifier = Modifier.fillMaxWidth().weight(WeightExtraHeavy),
                  elevation = CardDefaults.cardElevation(defaultElevation = DefaultCardElevation),
                  shape = RoundedCornerShape(CornerRadiusLarge)) {
                    MemberSelectionList(
                        members = candidates,
                        selectedMembers = selectedMembers,
                        onSelectionChanged = { newSelection ->
                          selectedMembers = newSelection
                          newSelection.firstOrNull()?.let { onMemberSelected(it) }
                        },
                        searchTestTag = ReplacementOrganizeTestTags.SEARCH_BAR,
                        listTestTag = ReplacementOrganizeTestTags.MEMBER_LIST,
                        summaryTestTag = ReplacementOrganizeTestTags.SELECTED_MEMBER_INFO,
                        isSingleSelection = true,
                    )
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
