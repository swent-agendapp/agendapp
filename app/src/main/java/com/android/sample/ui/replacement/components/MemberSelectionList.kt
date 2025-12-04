package com.android.sample.ui.replacement.components

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

// the complexity was reduced with the help of IA
data class MemberSelectionListOptions(
    val isSingleSelection: Boolean = false,
    val highlightColor: Color = CircusPalette.Primary.copy(alpha = 0.9f),
    val searchTestTag: String? = null,
    val listTestTag: String? = null,
    val summaryTestTag: String? = null,
    val memberTagBuilder: ((String) -> String)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionList(
    members: List<String>,
    selectedMembers: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    options: MemberSelectionListOptions = MemberSelectionListOptions(),
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredMembers =
      remember(searchQuery, members) {
        members.filter { it.contains(searchQuery, ignoreCase = true) }
      }

  Column(modifier = modifier.fillMaxSize()) {
    MemberSearchBar(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        options = options,
    )

    MemberSelectionLazyList(
        members = filteredMembers,
        selectedMembers = selectedMembers,
        onSelectionChanged = onSelectionChanged,
        options = options,
        modifier = Modifier.weight(WeightVeryHeavy))

    MemberSelectionSummary(
        selectedMembers = selectedMembers,
        summaryTestTag = options.summaryTestTag,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    options: MemberSelectionListOptions,
    modifier: Modifier = Modifier,
) {
  TextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = { Text(text = stringResource(R.string.search_member)) },
      modifier =
          modifier.fillMaxWidth().let { base ->
            if (options.searchTestTag != null) base.testTag(options.searchTestTag) else base
          },
      singleLine = true,
      trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
      shape = RoundedCornerShape(CornerRadiusLarge),
  )
}

@Composable
private fun MemberSelectionLazyList(
    members: List<String>,
    selectedMembers: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    options: MemberSelectionListOptions,
    modifier: Modifier = Modifier,
) {
  LazyColumn(
      modifier =
          modifier.fillMaxWidth().let { base ->
            if (options.listTestTag != null) base.testTag(options.listTestTag) else base
          },
      verticalArrangement = Arrangement.Top) {
        items(members) { member ->
          val isSelected = member in selectedMembers

          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .background(
                          if (isSelected) options.highlightColor else Color.White,
                      )
                      .clickable {
                        val newSelection =
                            calculateNewSelection(
                                isSingleSelection = options.isSingleSelection,
                                isSelected = isSelected,
                                member = member,
                                selectedMembers = selectedMembers,
                            )
                        onSelectionChanged(newSelection)
                      }
                      .padding(vertical = PaddingMedium)
                      .let { base ->
                        if (options.memberTagBuilder != null) {
                          base.testTag(options.memberTagBuilder.invoke(member))
                        } else {
                          base
                        }
                      },
              contentAlignment = Alignment.Center) {
                Text(text = member, textAlign = TextAlign.Center)
              }

          HorizontalDivider(
              thickness = DividerDefaults.Thickness,
              color = DividerDefaults.color,
          )
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberSelectionSummary(
    selectedMembers: Set<String>,
    summaryTestTag: String?,
    modifier: Modifier = Modifier,
) {
  val selectedMembersText =
      if (selectedMembers.isEmpty()) {
        stringResource(R.string.replacement_selected_members_none)
      } else {
        pluralStringResource(
            R.plurals.replacement_selected_members,
            selectedMembers.size,
            selectedMembers.joinToString(", "),
        )
      }

  OutlinedTextField(
      value = selectedMembersText,
      onValueChange = {},
      modifier =
          modifier.fillMaxWidth().let { base ->
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

private fun calculateNewSelection(
    isSingleSelection: Boolean,
    isSelected: Boolean,
    member: String,
    selectedMembers: Set<String>,
): Set<String> {
  return if (isSingleSelection) {
    if (isSelected) emptySet() else setOf(member)
  } else {
    if (isSelected) selectedMembers - member else selectedMembers + member
  }
}
