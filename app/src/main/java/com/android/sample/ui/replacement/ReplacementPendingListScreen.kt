package com.android.sample.ui.replacement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.pendingReplacements
import com.android.sample.ui.calendar.components.TopTitleBar
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingMedium
import java.time.format.DateTimeFormatter

object ReplacementPendingTestTags {
  const val SCREEN = "replacement_pending_screen"
  const val LIST = "replacement_pending_list"
  const val ITEM_PREFIX = "replacement_pending_item_"

  fun itemTag(id: String): String = ITEM_PREFIX + id
}

/**
 * Displays the list of pending replacements
 *
 * @param replacements The list of replacements to display
 */
@Composable
fun ReplacementPendingListScreen(
    replacements: List<Replacement> = getMockReplacements().pendingReplacements()
) {
  Scaffold(
      topBar = { TopTitleBar(title = stringResource(id = R.string.replacement_requests_title)) }) {
          paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(PaddingMedium)
                    .testTag(ReplacementPendingTestTags.SCREEN)) {
              Text(
                  text = stringResource(id = R.string.replacement_requests_subtitle),
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
              Spacer(modifier = Modifier.height(SpacingMedium))
              LazyColumn(
                  modifier = Modifier.fillMaxSize().testTag(ReplacementPendingTestTags.LIST),
                  verticalArrangement = Arrangement.spacedBy(SpacingMedium)) {
                    items(replacements, key = { it.id }) { replacement ->
                      ReplacementPendingCard(replacement = replacement)
                    }
                  }
            }
      }
}

/**
 * Display a single pending replacement card
 *
 * @param replacement The replacement request to display
 */
@Composable
fun ReplacementPendingCard(replacement: Replacement) {
  val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  val dateText = replacement.event.startLocalDate.format(dateFormatter)
  val timeText =
      "${replacement.event.startLocalTime.format(timeFormatter)} - " +
          replacement.event.endLocalTime.format(timeFormatter)

  Card(
      modifier =
          Modifier.fillMaxWidth().testTag(ReplacementPendingTestTags.itemTag(replacement.id)),
      shape = RoundedCornerShape(CornerRadiusLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(PaddingMedium)) {
          Text(
              text = replacement.event.title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "$dateText • $timeText", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(SpacingMedium))
          Text(
              text =
                  stringResource(
                      id = R.string.replacement_substituted_label, replacement.substitutedUserId),
              style = MaterialTheme.typography.bodySmall)
          Text(
              text =
                  stringResource(
                      id = R.string.replacement_substitute_label, replacement.substituteUserId),
              style = MaterialTheme.typography.bodySmall)
        }
      }
}
