package com.android.sample.ui.replacement.mainPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.eventSummaryComponents.ColoredSideBar
import com.android.sample.ui.common.MainPageTopBar
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.common.SecondaryButton
import com.android.sample.ui.theme.*

// Assisted by AI

object ReplacementEmployeeListTestTags {
  const val ROOT = "replacement_employee_list_root"
  const val ASK_BUTTON = "replacement_employee_ask_button"
  const val SELECT_EVENT_BUTTON = "replacement_employee_select_event_button"
  const val DATE_RANGE_BUTTON = "replacement_employee_date_range_button"
  const val CARD_SIDE_BAR = "card_side_bar"

  fun card(id: String) = "replacement_employee_card_$id"

  fun accept(id: String) = "replacement_employee_accept_$id"

  fun refuse(id: String) = "replacement_employee_refuse_$id"
}

data class ReplacementRequestUi(
    val id: String,
    val weekdayAndDay: String,
    val timeRange: String,
    val title: String,
    val description: String,
    val absentDisplayName: String,
    val color: Color
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
            absentDisplayName = "Emilien",
            color = EventPalette.Blue),
        ReplacementRequestUi(
            id = "req2",
            weekdayAndDay = "Friday 7",
            timeRange = "14:00 - 16:00",
            title = "Meeting 321",
            description = "Meeting about 321",
            absentDisplayName = "Emilien",
            color = EventPalette.Purple))

data class ReplacementEmployeeCallbacks(
    val onAccept: (String) -> Unit = {},
    val onRefuse: (String) -> Unit = {},
)

data class ReplacementAdminActions(
    val onOrganizeClick: () -> Unit = {},
    val onWaitingConfirmationClick: () -> Unit = {},
    val onConfirmedClick: () -> Unit = {},
)

data class ReplacementCreateRequestActions(
    val onSelectEvent: () -> Unit = {},
    val onChooseDateRange: () -> Unit = {},
)

@Composable
fun ReplacementEmployeeListScreen(
    requests: List<ReplacementRequestUi> = sampleRequests,
    callbacks: ReplacementEmployeeCallbacks = ReplacementEmployeeCallbacks(),
    isAdmin: Boolean = false,
    adminActions: ReplacementAdminActions = ReplacementAdminActions(),
    createRequestActions: ReplacementCreateRequestActions = ReplacementCreateRequestActions(),
) {
  var showCreateOptions by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        MainPageTopBar(
            title = stringResource(R.string.replacement_title),
        )
      },
      bottomBar = {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
          HorizontalDivider(
              thickness = BorderWidthThin,
              color = MaterialTheme.colorScheme.outlineVariant,
          )
        }
        Column(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = PaddingSmall, vertical = PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingExtraSmall),
        ) {
          if (isAdmin) { // change to if is admin when implemented
            SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.organize_replacement),
                onClick = adminActions.onOrganizeClick,
            )

            SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.waiting_confirmation_replacement),
                onClick = adminActions.onWaitingConfirmationClick,
            )

            SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.confirmed_replacements),
                onClick = adminActions.onConfirmedClick,
            )
          }
          AnimatedVisibility(visible = showCreateOptions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PaddingSmall),
            ) {
              SecondaryButton(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(ReplacementEmployeeListTestTags.SELECT_EVENT_BUTTON),
                  text = stringResource(R.string.replacement_create_select_event),
                  onClick = {
                    showCreateOptions = false
                    createRequestActions.onSelectEvent()
                  },
              )

              SecondaryButton(
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(ReplacementEmployeeListTestTags.DATE_RANGE_BUTTON),
                  text = stringResource(R.string.replacement_create_choose_date_range),
                  onClick = {
                    showCreateOptions = false
                    createRequestActions.onChooseDateRange()
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
                    .padding(horizontal = PaddingSmall, vertical = PaddingMedium)
                    .testTag(ReplacementEmployeeListTestTags.ROOT),
            contentPadding =
                PaddingValues(
                    top = PaddingMedium,
                    bottom = PaddingExtraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(PaddingSmall)) {
              if (requests.isEmpty()) {
                item {
                  Text(
                      text = stringResource(R.string.replacement_no_incoming_requests),
                      style = MaterialTheme.typography.bodyMedium,
                      textAlign = TextAlign.Center,
                  )
                  Spacer(Modifier.height(SpacingMedium))
                }
              } else {
                items(requests, key = { it.id }) { req ->
                  ReplacementRequestCard(
                      data = req,
                      onAccept = { callbacks.onAccept(req.id) },
                      onRefuse = { callbacks.onRefuse(req.id) },
                      testTag = ReplacementEmployeeListTestTags.card(req.id),
                      acceptTag = ReplacementEmployeeListTestTags.accept(req.id),
                      refuseTag = ReplacementEmployeeListTestTags.refuse(req.id),
                  )
                }
              }

              item { Spacer(Modifier.height(SpacingMedium)) }
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
  val accentColor = CircusPalette.Tertiary

  Card(
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadiusLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = ElevationNull),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
      border = CardDefaults.outlinedCardBorder(),
  ) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      ColoredSideBar(
          width = BarWidthMedium,
          color = data.color,
          shape = RoundedCornerShape(CornerRadiusLarge),
          testTag = ReplacementEmployeeListTestTags.CARD_SIDE_BAR)
      Column(
          modifier =
              Modifier.weight(WeightExtraHeavy)
                  .fillMaxWidth()
                  .padding(horizontal = PaddingMedium, vertical = PaddingSmall),
          verticalArrangement = Arrangement.spacedBy(SpacingSmall),
      ) {
        Text(
            text = data.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PaddingSmall),
        ) {
          Icon(
              imageVector = Icons.Filled.AccessTime,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
              text = "${data.weekdayAndDay} • ${data.timeRange}",
              style = MaterialTheme.typography.bodyMedium,
          )
        }

        Text(
            text =
                stringResource(
                    R.string.replacement_substituted_label,
                    data.absentDisplayName,
                ),
            style = MaterialTheme.typography.bodySmall,
        )

        if (data.description.isNotBlank()) {
          Text(
              text = data.description,
              style = MaterialTheme.typography.bodySmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )
        }

        Spacer(Modifier.height(PaddingExtraSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PaddingSmall),
        ) {
          OutlinedButton(
              onClick = onRefuse,
              modifier = Modifier.weight(1f).testTag(refuseTag),
              shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadiusLarge),
              border = BorderStroke(BorderWidthThin, CircusPalette.Primary),
              colors =
                  ButtonDefaults.outlinedButtonColors(
                      containerColor = MaterialTheme.colorScheme.background,
                      contentColor = MaterialTheme.colorScheme.onSurface,
                  ),
          ) {
            Text(text = androidx.compose.ui.res.stringResource(R.string.replacement_refuse_short))
          }

          OutlinedButton(
              onClick = onAccept,
              modifier = Modifier.weight(1f).testTag(acceptTag),
              shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadiusLarge),
              border = BorderStroke(BorderWidthThin, accentColor),
              colors =
                  ButtonDefaults.outlinedButtonColors(
                      containerColor = MaterialTheme.colorScheme.background,
                      contentColor = MaterialTheme.colorScheme.onSurface,
                  ),
          ) {
            Text(text = androidx.compose.ui.res.stringResource(R.string.replacement_accept_short))
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ReplacementEmployeeViewScreenPreview() {
  ReplacementEmployeeListScreen()
}
