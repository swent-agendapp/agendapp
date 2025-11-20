package com.android.sample.ui.components

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
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.android.sample.R
import com.android.sample.ui.theme.CircusPalette
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightVeryHeavy

// Kdoc writen with the help of AI
/**
 * Reusable component for selecting one or multiple members from a searchable list.
 *
 * It contains:
 * - a search bar
 * - a scrollable selectable list
 * - a read-only summary field for the selection
 *
 * It is used by:
 * - [SelectSubstitutedScreen]
 * - [ProcessReplacementScreen]
 *
 * Behavior:
 * - If [isSingleSelection] is true → only one element can be selected at a time.
 * - If [isSingleSelection] is false → multiple selection (toggle behavior).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionList(
    members: List<String>,
    selectedMembers: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    isSingleSelection: Boolean = false,
    highlightColor: Color = CircusPalette.Primary.copy(alpha = 0.9f),
    searchTestTag: String? = null,
    listTestTag: String? = null,
    summaryTestTag: String? = null,
    memberTagBuilder: ((String) -> String)? = null,
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredMembers =
      remember(searchQuery, members) {
        members.filter { it.contains(searchQuery, ignoreCase = true) }
      }

  Column(modifier = modifier.fillMaxSize()) {
    TextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text(text = stringResource(R.string.search_member)) },
        modifier =
            Modifier.fillMaxWidth().let { base ->
              if (searchTestTag != null) base.testTag(searchTestTag) else base
            },
        singleLine = true,
        trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(CornerRadiusLarge))

    LazyColumn(
        modifier =
            Modifier.weight(WeightVeryHeavy).fillMaxWidth().let { base ->
              if (listTestTag != null) base.testTag(listTestTag) else base
            },
        verticalArrangement = Arrangement.Top) {
          items(filteredMembers) { member ->
            val isSelected = member in selectedMembers

            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(if (isSelected) highlightColor else Color.White)
                        .clickable {
                          val newSelection =
                              if (isSingleSelection) {
                                if (isSelected) emptySet() else setOf(member)
                              } else {
                                if (isSelected) selectedMembers - member
                                else selectedMembers + member
                              }
                          onSelectionChanged(newSelection)
                        }
                        .padding(vertical = PaddingMedium)
                        .let { base ->
                          if (memberTagBuilder != null) {
                            base.testTag(memberTagBuilder(member))
                          } else {
                            base
                          }
                        },
                contentAlignment = Alignment.Center) {
                  Text(text = member, textAlign = TextAlign.Center)
                }

            HorizontalDivider(thickness = DividerDefaults.Thickness, color = DividerDefaults.color)
          }
        }

    val selectedMembersText =
        if (selectedMembers.isEmpty()) {
          stringResource(R.string.replacement_selected_members_none)
        } else {
          pluralStringResource(
              R.plurals.replacement_selected_members,
              selectedMembers.size,
              selectedMembers.joinToString(", "))
        }

    OutlinedTextField(
        value = selectedMembersText,
        onValueChange = {},
        modifier =
            Modifier.fillMaxWidth().let { base ->
              if (summaryTestTag != null) base.testTag(summaryTestTag) else base
            },
        singleLine = true,
        shape = RoundedCornerShape(CornerRadiusLarge),
        readOnly = true,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
            ),
    )
  }
}
