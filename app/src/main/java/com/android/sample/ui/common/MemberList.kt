package com.android.sample.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.DefaultCardElevation
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.WeightExtraHeavy
import com.android.sample.ui.theme.WeightVeryHeavy

// Assisted by AI

/** Test tags for MemberList composable. */
object MemberListTestTags {
  const val MEMBER_SEARCH_BAR = "member_search_bar"

  fun memberItemTag(memberId: String): String = "member_item_$memberId"
}

/**
 * A composable that displays a list of members with a search bar.
 *
 * @param members The list of members to display.
 * @param admins The list of admin members.
 * @param onMemberClick Callback invoked when a member is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun MemberList(
    modifier: Modifier = Modifier,
    members: List<User> = emptyList(),
    admins: List<User> = emptyList(),
    onMemberClick: (User) -> Unit = {},
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredMembers =
      remember(searchQuery, members) {
        members.filter { it.display().contains(searchQuery, ignoreCase = true) }
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
        Column(modifier = Modifier.fillMaxSize()) {
          MemberSearchBar(
              searchQuery = searchQuery,
              onSearchQueryChange = { searchQuery = it },
          )

          MemberLazyList(
              members = filteredMembers,
              admins = admins,
              modifier = Modifier.weight(WeightVeryHeavy),
              onMemberClick = onMemberClick)
        }
      }
}

/**
 * A composable that displays a search bar for filtering members.
 *
 * @param searchQuery The current search query.
 * @param onSearchQueryChange Callback invoked when the search query changes.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
private fun MemberSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  TextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = { Text(text = stringResource(R.string.search_member)) },
      modifier = modifier.fillMaxWidth().testTag(MemberListTestTags.MEMBER_SEARCH_BAR),
      singleLine = true,
      trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
      shape = RoundedCornerShape(CornerRadiusLarge),
  )
}

/**
 * A composable that displays a list of members.
 *
 * @param members The list of members to display.
 * @param onMemberClick Callback invoked when a member is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
private fun MemberLazyList(
    modifier: Modifier = Modifier,
    members: List<User> = emptyList(),
    admins: List<User> = emptyList(),
    onMemberClick: (User) -> Unit = {},
) {
  LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
    items(members) { member ->
      val isAdmin = admins.any { it.id == member.id }

      Box(
          modifier =
              modifier
                  .fillMaxWidth()
                  .background(Color.White)
                  .clickable { onMemberClick(member) }
                  .padding(vertical = PaddingMedium)
                  .testTag(MemberListTestTags.memberItemTag(member.id)),
          contentAlignment = Alignment.CenterStart) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = PaddingMedium),
                verticalAlignment = Alignment.CenterVertically) {
                  Text(text = member.display(), textAlign = TextAlign.Center)

                  Spacer(modifier = Modifier.weight(WeightExtraHeavy))

                  if (isAdmin) {
                    Text(
                        text = stringResource(R.string.admin),
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray)
                  }
                }
          }
      HorizontalDivider(
          thickness = DividerDefaults.Thickness,
          color = DividerDefaults.color,
      )
    }
  }
}
