package com.android.sample.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightFadeEffect
import com.android.sample.ui.theme.WeightVeryHeavy

// the complexity was reduced with the help of IA
data class MemberSelectionListOptions(
    val isSingleSelection: Boolean = false,
    val highlightColor: Color = GeneralPalette.Secondary.copy(alpha = 0.9f),
    val searchTestTag: String? = null,
    val listTestTag: String? = null,
    val summaryTestTag: String? = null,
    val memberTagBuilder: ((String) -> String)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionList(
    modifier: Modifier = Modifier,
    members: List<User> = emptyList(),
    selectedMembers: Set<User> = emptySet(),
    onSelectionChanged: (Set<User>) -> Unit = {},
    options: MemberSelectionListOptions = MemberSelectionListOptions(),
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredMembers =
      remember(searchQuery, members) {
        members.filter { it -> it.display().contains(searchQuery, ignoreCase = true) }
      }

  Card(
      modifier = modifier.fillMaxWidth(),
      colors =
          CardColors(
              containerColor = GeneralPalette.CardContainer,
              contentColor = GeneralPalette.OnSurface,
              disabledContainerColor = GeneralPalette.CardContainer,
              disabledContentColor = GeneralPalette.OnSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = DefaultCardElevation),
      shape = RoundedCornerShape(CornerRadiusLarge)) {
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
    members: List<User>,
    selectedMembers: Set<User>,
    onSelectionChanged: (Set<User>) -> Unit,
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
                          base.testTag(options.memberTagBuilder.invoke(member.display()))
                        } else {
                          base
                        }
                      },
              contentAlignment = Alignment.CenterStart) {
                Text(
                    text = member.display(),
                    modifier = modifier.padding(start = PaddingMedium),
                    textAlign = TextAlign.Center)
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
    selectedMembers: Set<User>,
    summaryTestTag: String?,
    modifier: Modifier = Modifier,
) {
  val selectedMembersText =
      if (selectedMembers.isEmpty()) {
        stringResource(R.string.replacement_selected_members_none)
      } else {
        pluralStringResource(
            R.plurals.replacement_selected_members,
            count = selectedMembers.size,
            selectedMembers.joinToString(
                separator = ", ",
                // extra space as a postfix to be able to scroll enough and not see the fade effect
                // at the end of the last name
                postfix = "      ") { it ->
                  it.display()
                },
        )
      }

  var boxWidth by remember { mutableIntStateOf(0) } // used to adapt Fade start

  Box(
      modifier =
          modifier
              .fillMaxWidth()
              .background(
                  color = GeneralPalette.CardContainer,
                  shape = RoundedCornerShape(CornerRadiusLarge))
              .padding(horizontal = PaddingMedium, vertical = PaddingMedium)
              .onSizeChanged { boxWidth = it.width }) {
        val scrollState = rememberScrollState()

        // Scrollable text
        Text(
            text = selectedMembersText,
            maxLines = 1,
            modifier =
                Modifier.horizontalScroll(scrollState).align(Alignment.CenterStart).let { base ->
                  if (summaryTestTag != null) base.testTag(summaryTestTag) else base
                },
        )

        // Fade on the right of the Text
        Box(
            modifier =
                Modifier.matchParentSize()
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        GeneralPalette.Transparent, GeneralPalette.CardContainer),
                                startX = boxWidth * WeightFadeEffect)))
      }
}

private fun calculateNewSelection(
    isSingleSelection: Boolean,
    isSelected: Boolean,
    member: User,
    selectedMembers: Set<User>,
): Set<User> {
  return if (isSingleSelection) {
    if (isSelected) emptySet() else setOf(member)
  } else {
    if (isSelected) selectedMembers - member else selectedMembers + member
  }
}
