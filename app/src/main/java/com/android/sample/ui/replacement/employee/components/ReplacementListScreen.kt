package com.android.sample.ui.replacement.employee.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.*

// Assisted by AI

private val titleMaxLine = 1
private val descriptionMaxLine = 2
/** ---------- Test Tags ---------- */
object ReplacementEmployeeListTestTags {
  const val ROOT = "replacement_employee_list_root"
  const val ASK_BUTTON = "replacement_employee_ask_button"

  fun card(id: String) = "replacement_employee_card_$id"

  fun accept(id: String) = "replacement_employee_accept_$id"

  fun refuse(id: String) = "replacement_employee_refuse_$id"
}

/** ---------- Simple UI structure (remove after VM is implemented) ---------- */
data class ReplacementRequestUi(
    val id: String,
    val weekdayAndDay: String,
    val timeRange: String,
    val title: String,
    val description: String,
    val absentDisplayName: String
)

/** Sample Requests List (only for preview) */
private val sampleRequests =
    listOf(
        ReplacementRequestUi(
            id = "req1",
            weekdayAndDay = "Monday 7",
            timeRange = "10:00 - 12:00",
            title = "Meeting 123",
            description = "Meeting about 123",
            absentDisplayName = "Emilien"),
        ReplacementRequestUi(
            id = "req2",
            weekdayAndDay = "Friday 7",
            timeRange = "14:00 - 16:00",
            title = "Meeting 321",
            description = "Meeting about 321",
            absentDisplayName = "Emilien"))

@Composable
fun ReplacementEmployeeListScreen(
    requests: List<ReplacementRequestUi> = sampleRequests,
    onAccept: (id: String) -> Unit = {},
    onRefuse: (id: String) -> Unit = {},
    onSelectEvent: () -> Unit = {},
    onChooseDateRange: () -> Unit = {},
    onBack: () -> Unit = {},
) {
  var showCreateOptions by remember { mutableStateOf(false) }
  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.replacement_title),
            onClick = onBack,
        )
      },
      bottomBar = {
        Column(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = PaddingLarge, vertical = PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingMedium),
        ) {
          AnimatedVisibility(visible = showCreateOptions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingSmall),
            ) {
              SecondaryButton(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(ReplacementEmployeeCreateTestTags.SELECT_EVENT_BUTTON),
                  text = stringResource(R.string.replacement_create_select_event),
                  onClick = {
                    showCreateOptions = false
                    onSelectEvent()
                  },
              )

              SecondaryButton(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(ReplacementEmployeeCreateTestTags.CHOOSE_DATE_RANGE_BUTTON),
                  text = stringResource(R.string.replacement_create_choose_date_range),
                  onClick = {
                    showCreateOptions = false
                    onChooseDateRange()
                  },
              )
            }
          }

          PrimaryButton(
              onClick = { showCreateOptions = !showCreateOptions },
              text = stringResource(R.string.replacement_ask_to_be_replaced),
              modifier =
                  Modifier.fillMaxWidth().testTag(ReplacementEmployeeListTestTags.ASK_BUTTON),
          )
        }
      }) { inner ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = PaddingLarge)
                    .testTag(ReplacementEmployeeListTestTags.ROOT),
            verticalArrangement = Arrangement.spacedBy(PaddingLarge)) {
              items(requests, key = { it.id }) { req ->
                ReplacementRequestCard(
                    data = req,
                    onAccept = { onAccept(req.id) },
                    onRefuse = { onRefuse(req.id) },
                    testTag = ReplacementEmployeeListTestTags.card(req.id),
                    acceptTag = ReplacementEmployeeListTestTags.accept(req.id),
                    refuseTag = ReplacementEmployeeListTestTags.refuse(req.id),
                )
              }

              item { Spacer(Modifier.height(heightLarge)) }
            }
      }
}

@Composable
private fun ReplacementRequestCard(
    data: ReplacementRequestUi,
    onAccept: () -> Unit,
    onRefuse: () -> Unit,
    testTag: String,
    acceptTag: String,
    refuseTag: String,
) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      shape = RoundedCornerShape(CornerRadiusLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = ElevationLow),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(PaddingLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
          modifier =
              Modifier.fillMaxHeight()
                  .width(BarWidthSmall)
                  .background(MaterialTheme.colorScheme.primary),
      )

      Spacer(Modifier.width(PaddingLarge))

      Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(SpacingSmall),
      ) {
        Text(
            text = data.title,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            maxLines = titleMaxLine,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PaddingMedium),
        ) {
          Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
              text = "${data.weekdayAndDay} • ${data.timeRange}",
              style = MaterialTheme.typography.bodyMedium,
          )
        }

        Text(
            text = stringResource(R.string.replacement_substituted_label, data.absentDisplayName),
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = descriptionMaxLine,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(PaddingMedium))

        Row(horizontalArrangement = Arrangement.spacedBy(PaddingLarge)) {
          TextButton(onClick = onAccept, modifier = Modifier.testTag(acceptTag)) {
            Text(text = stringResource(R.string.replacement_accept_short))
          }
          TextButton(onClick = onRefuse, modifier = Modifier.testTag(refuseTag)) {
            Text(text = stringResource(R.string.replacement_refuse_short))
          }
        }
      }
    }
  }
}

/** ---------- Previews ---------- */
@Preview(showBackground = true)
@Composable
fun ReplacementEmployeeViewScreenPreview() {
  ReplacementEmployeeListScreen()
}
