package com.android.sample.ui.replacement

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.toProcessReplacements
import com.android.sample.model.replacement.waitingForAnswerAndDeclinedReplacements
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import java.time.format.DateTimeFormatter

object ReplacementPendingTestTags {
  const val SCREEN = "replacement_pending_screen"
  const val LIST = "replacement_pending_list"
  const val ITEM_PREFIX = "replacement_pending_item_"

  fun itemTag(id: String): String = ITEM_PREFIX + id
}

// That function (ReplacementAssistChip) was created with the help of IA
@Composable
private fun ReplacementAssistChip(
    count: Int,
    @StringRes labelRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
  AssistChip(
      enabled = enabled,
      onClick = onClick,
      label = { Text(stringResource(id = labelRes, count)) },
      leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = tint) })
}

/**
 * Pending replacements screen (admin side)
 *
 * Two sections:
 * - replacements to process (admin must choose someone)
 * - replacements waiting for an answer from the substitute(s)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplacementPendingListScreen(
    replacementsToProcess: List<Replacement> = getMockReplacements().toProcessReplacements(),
    replacementsWaitingForAnswer: List<Replacement> =
        getMockReplacements().waitingForAnswerAndDeclinedReplacements(),
    onProcessReplacement: (Replacement) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(id = R.string.replacement_requests_title)) },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back))
              }
            })
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(PaddingMedium)
                    .testTag(ReplacementPendingTestTags.SCREEN)) {
              LazyColumn(
                  modifier = Modifier.fillMaxSize().testTag(ReplacementPendingTestTags.LIST),
                  verticalArrangement = Arrangement.spacedBy(SpacingMedium)) {
                    if (replacementsToProcess.isNotEmpty()) {
                      item {
                        Text(
                            text =
                                stringResource(id = R.string.replacement_to_process_section_title),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold))
                        Spacer(modifier = Modifier.height(SpacingSmall))
                      }

                      items(replacementsToProcess, key = { it.id }) { replacement ->
                        ReplacementToProcessCard(
                            replacement = replacement,
                            onProcessClick = { onProcessReplacement(replacement) })
                      }
                    }

                    if (replacementsWaitingForAnswer.isNotEmpty()) {
                      item {
                        Spacer(modifier = Modifier.height(SpacingLarge))
                        Text(
                            text =
                                stringResource(
                                    id = R.string.replacement_waiting_answer_section_title),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold))
                        Spacer(modifier = Modifier.height(SpacingSmall))
                      }

                      val groupedWaiting =
                          replacementsWaitingForAnswer
                              .groupBy { it.event to it.absentUserId }
                              .values
                              .toList()

                      items(groupedWaiting) { group ->
                        ReplacementWaitingCard(replacements = group)
                      }
                    }
                  }
            }
      }
}

/** Card for a replacement that still needs to be processed by the admin */
@Composable
private fun ReplacementToProcessCard(
    replacement: Replacement,
    onProcessClick: () -> Unit,
) {
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
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
          Spacer(modifier = Modifier.height(SpacingSmall))
          Text(text = "$dateText • $timeText", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(SpacingMedium))
          Text(
              text =
                  stringResource(
                      id = R.string.replacement_substituted_label, replacement.absentUserId),
              style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(SpacingSmall))

          PrimaryButton(
              onClick = onProcessClick,
              text = stringResource(id = R.string.replacement_process_button),
              innerPadding = PaddingExtraSmall)
        }
      }
}

/** Card for a pending replacement that is already waiting for a substitute's answer */
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
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
          Spacer(modifier = Modifier.height(SpacingSmall))
          Text(text = "$dateText • $timeText", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(SpacingMedium))
          Text(
              text =
                  stringResource(
                      id = R.string.replacement_substituted_label, replacement.absentUserId),
              style = MaterialTheme.typography.bodySmall)
          Text(
              text =
                  stringResource(
                      id = R.string.replacement_substitute_label, replacement.substituteUserId),
              style = MaterialTheme.typography.bodySmall)
        }
      }
}

@Composable
fun ReplacementWaitingCard(replacements: List<Replacement>) {
  if (replacements.isEmpty()) return

  val first = replacements.first()
  val event = first.event
  val absentUserId = first.absentUserId

  val pending =
      replacements.filter {
        it.status == com.android.sample.model.replacement.ReplacementStatus.WaitingForAnswer
      }
  val declined =
      replacements.filter {
        it.status == com.android.sample.model.replacement.ReplacementStatus.Declined
      }

  var showPendingDialog by remember { mutableStateOf(false) }
  var showDeclinedDialog by remember { mutableStateOf(false) }

  val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  val dateText = event.startLocalDate.format(dateFormatter)
  val timeText =
      "${event.startLocalTime.format(timeFormatter)} - ${event.endLocalTime.format(timeFormatter)}"

  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(CornerRadiusLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(PaddingMedium)) {
          Text(
              text = event.title,
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
          Spacer(modifier = Modifier.height(SpacingSmall))
          Text(text = "$dateText • $timeText", style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(SpacingMedium))
          Text(
              text = stringResource(R.string.replacement_substituted_label, absentUserId),
              style = MaterialTheme.typography.bodySmall)

          Spacer(modifier = Modifier.height(SpacingMedium))

          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReplacementAssistChip(
                count = pending.size,
                labelRes = R.string.replacement_no_response_label,
                enabled = pending.isNotEmpty(),
                onClick = { showPendingDialog = true },
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                tint = MaterialTheme.colorScheme.tertiary)

            ReplacementAssistChip(
                count = declined.size,
                labelRes = R.string.replacement_declined_label,
                enabled = declined.isNotEmpty(),
                onClick = { showDeclinedDialog = true },
                icon = Icons.Outlined.Close,
                tint = MaterialTheme.colorScheme.error)
          }
        }
      }

  if (showPendingDialog) {
    PeopleListDialog(
        title = stringResource(R.string.replacement_pending_people_title),
        people = pending.map { it.substituteUserId },
        onDismiss = { showPendingDialog = false })
  }
  if (showDeclinedDialog) {
    PeopleListDialog(
        title = stringResource(R.string.replacement_declined_people_title),
        people = declined.map { it.substituteUserId },
        onDismiss = { showDeclinedDialog = false })
  }
}

@Composable
private fun PeopleListDialog(title: String, people: List<String>, onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(title) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (people.isEmpty()) {
            Text("—")
          } else {
            people.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.replacement_people_dialog_close))
        }
      })
}

@Preview(showBackground = true)
@Composable
fun ReplacementPendingListScreenPreview() {
  ReplacementPendingListScreen()
}
