package com.android.sample.ui.replacement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.calendar.utils.DateTimeUtils.DATE_FORMAT_PATTERN
import com.android.sample.ui.theme.BarWidthSmall
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SmallCardElevation
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import java.time.Instant
import java.time.format.DateTimeFormatter

object ReplacementUpcomingTestTags {
  const val SCREEN = "replacement_upcoming_screen"
  const val LIST = "replacement_upcoming_list"
  private const val ITEM_PREFIX = "replacement_upcoming_item_"

  fun itemTag(id: String): String = ITEM_PREFIX + id
}

/**
 * Screen showing future confirmed replacements
 *
 * A replacement is considered "upcoming" if:
 * - it's status is [ReplacementStatus.Accepted]
 * - it's event start date is in the future
 *
 *     @param replacements List of replacements to display
 *     @param onNavigateBack Callback invoked when the user presses the back button in the top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplacementUpcomingListScreen(
    replacements: List<Replacement> = getMockReplacements().filterUpcomingAccepted(),
    onNavigateBack: () -> Unit = {},
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text =
                      androidx.compose.ui.res.stringResource(R.string.replacement_upcoming_title))
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription =
                        androidx.compose.ui.res.stringResource(R.string.common_back))
              }
            })
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(PaddingMedium)
                    .testTag(ReplacementUpcomingTestTags.SCREEN)) {
              LazyColumn(
                  modifier = Modifier.fillMaxSize().testTag(ReplacementUpcomingTestTags.LIST),
                  verticalArrangement = Arrangement.spacedBy(SpacingMedium)) {
                    items(replacements, key = { it.id }) { replacement ->
                      ReplacementUpcomingCard(replacement = replacement)
                    }
                  }
            }
      }
}

/** Card for a future confirmed replacement. */
@Composable
private fun ReplacementUpcomingCard(replacement: Replacement) {
  val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  val dateText = replacement.event.startLocalDate.format(dateFormatter)
  val timeText =
      "${replacement.event.startLocalTime.format(timeFormatter)} - " +
          replacement.event.endLocalTime.format(timeFormatter)

  Card(
      modifier =
          Modifier.fillMaxWidth().testTag(ReplacementUpcomingTestTags.itemTag(replacement.id)),
      shape = RoundedCornerShape(CornerRadiusLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = SmallCardElevation)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
              modifier =
                  Modifier.fillMaxHeight().width(BarWidthSmall).background(replacement.event.color),
          )

          Spacer(modifier = Modifier.width(SpacingMedium))

          Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(SpacingSmall),
          ) {
            Text(
                text = replacement.event.title,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                  imageVector = Icons.Filled.AccessTime,
                  contentDescription = null,
                  modifier = Modifier.padding(end = SpacingSmall),
              )
              Text(
                  text = "$dateText • $timeText",
                  style = MaterialTheme.typography.bodyMedium,
              )
            }

            Text(
                text =
                    androidx.compose.ui.res.stringResource(
                        id = R.string.replacement_substituted_label,
                        replacement.absentUserId,
                    ),
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text =
                    androidx.compose.ui.res.stringResource(
                        id = R.string.replacement_substitute_label,
                        replacement.substituteUserId,
                    ),
                style = MaterialTheme.typography.bodySmall,
            )
          }
        }
      }
}

/**
 * Helper to filter a list of [Replacement] and keep only:
 * - Accepted replacements
 * - Events strictly in the future (based on system clock)
 */
private fun List<Replacement>.filterUpcomingAccepted(
    now: Instant = Instant.now(),
): List<Replacement> =
    this.filter { replacement ->
      replacement.status == ReplacementStatus.Accepted && replacement.event.startDate.isAfter(now)
    }

@Preview(showBackground = true)
@Composable
private fun ReplacementUpcomingListScreenPreview() {
  ReplacementUpcomingListScreen(replacements = getMockReplacements())
}
