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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.PaddingExtraLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightVeryHeavy

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
 * @param onMemberSelected Callback invoked when a member is selected from the list. The selected
 *   member name is passed as a parameter.
 * @param onSelectEvents Called when the user confirms the member and wants to choose affected
 *   events.
 * @param onSelectDateRange Called when the user chooses to specify a time range first.
 * @param onBack Called when the user returns to the previous step (top bar back action).
 */
@Composable
fun SelectSubstitutedScreen(
    onMemberSelected: (String) -> Unit = {},
    onSelectEvents: () -> Unit = {},
    onSelectDateRange: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  var selectedMember by remember { mutableStateOf("") }
  val members =
      listOf(
          "Alice",
          "Bob",
          "Charlie",
          "David",
          "Eve",
          "Frank") // Placeholder for all possible participants

  // Search UI state
  var searchQuery by remember { mutableStateOf("") }

  // Filter the list when search changes
  val filteredMembers =
      remember(searchQuery) { members.filter { it.contains(searchQuery, ignoreCase = true) } }

  Scaffold(
      topBar = {
        TopTitleBar(
            title = stringResource(R.string.organize_replacement),
            canNavigateBack = true,
            onBack = onBack)
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
                  modifier = Modifier.padding(vertical = PaddingLarge))

              /** Scrollable selectable list * */
              Card(
                  modifier = Modifier.fillMaxWidth().weight(WeightVeryHeavy),
                  elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                  shape = RoundedCornerShape(CornerRadiusLarge)) {
                    Column(Modifier.fillMaxSize()) {

                      /** Search bar * */
                      TextField(
                          value = searchQuery,
                          onValueChange = { searchQuery = it },
                          placeholder = { Text(text = stringResource(R.string.search_member)) },
                          modifier = Modifier.fillMaxWidth(),
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
                          modifier = Modifier.weight(WeightVeryHeavy).fillMaxWidth(),
                          verticalArrangement = Arrangement.Top) {
                            items(filteredMembers) { member ->
                              val isSelected = member == selectedMember

                              Box(
                                  modifier =
                                      Modifier.fillMaxWidth()
                                          .background(if (isSelected) Color.Gray else Color.White)
                                          .clickable {
                                            onMemberSelected(member)
                                            selectedMember = member
                                          }
                                          .padding(vertical = PaddingMedium),
                                  contentAlignment = Alignment.Center) {
                                    Text(text = member, textAlign = TextAlign.Center)
                                  }

                              HorizontalDivider(
                                  thickness = DividerDefaults.Thickness,
                                  color = DividerDefaults.color)
                            }
                          }

                      /** Read-only selected member field * */
                      OutlinedTextField(
                          value = stringResource(R.string.selected_member, selectedMember),
                          onValueChange = {}, // ignored because readOnly
                          modifier = Modifier.fillMaxWidth(),
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
                    OutlinedButton(
                        onClick = onSelectEvents,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CornerRadiusLarge),
                        enabled = selectedMember.isNotEmpty()) {
                          Text(
                              text = stringResource(R.string.select_events),
                              modifier = Modifier.padding(PaddingMedium))
                        }

                    OutlinedButton(
                        onClick = onSelectDateRange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CornerRadiusLarge),
                        enabled = selectedMember.isNotEmpty()) {
                          Text(
                              text = stringResource(R.string.select_date_range),
                              modifier = Modifier.padding(PaddingMedium))
                        }
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
