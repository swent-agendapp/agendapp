package com.android.sample.ui.calendar.replacementEmployee.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.components.TopTitleBar
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
    val description: String
)

/** Sample Requests List (only for preview) */
private val sampleRequests =
    listOf(
        ReplacementRequestUi(
            id = "req1",
            weekdayAndDay = "Monday 7",
            timeRange = "10:00 - 12:00",
            title = "Meeting 123",
            description = "Meeting about 123"),
        ReplacementRequestUi(
            id = "req2",
            weekdayAndDay = "Friday 7",
            timeRange = "14:00 - 16:00",
            title = "Meeting 321",
            description = "Meeting about 321"))

/**
 * **ReplacementEmployeeListScreen**
 *
 * Displays a list of replacement requests for an employee, allowing them to **accept**, **refuse**,
 * or **create** a new replacement request.
 *
 * This screen is the main entry point for employees managing their replacement duties. It lists all
 * current replacement requests (provided by [ReplacementRequestUi]) and includes a bottom button to
 * initiate a new replacement request.
 * ---
 * ### Parameters:
 *
 * @param requests List of replacement requests displayed on screen.
 * @param onAccept Callback triggered when the user accepts a replacement request.
 * @param onRefuse Callback triggered when the user refuses a replacement request.
 * @param onAskToBeReplaced Callback triggered when the user clicks "Ask to be replaced" at the
 *   bottom.
 * ---
 * ### Test Tags:
 * - [ReplacementEmployeeListTestTags.ROOT] — root column container.
 * - [ReplacementEmployeeListTestTags.ASK_BUTTON] — bottom "Ask to be replaced" button.
 * - [ReplacementEmployeeListTestTags.card] — individual request card container.
 * - [ReplacementEmployeeListTestTags.accept] — accept button inside each card.
 * - [ReplacementEmployeeListTestTags.refuse] — refuse button inside each card.
 */
@Composable
fun ReplacementEmployeeListScreen(
    requests: List<ReplacementRequestUi> = sampleRequests, // Will be provided by ViewModel later
    onAccept: (id: String) -> Unit = {},
    onRefuse: (id: String) -> Unit = {},
    onAskToBeReplaced: () -> Unit = {}
) {
  Scaffold(
      topBar = { TopTitleBar(title = stringResource(R.string.replacement_title)) },
      bottomBar = {
        // centered large button at the bottom
        Box(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = PaddingLarge, vertical = PaddingLarge),
            contentAlignment = Alignment.Center) {
              // "Ask to be replaced" button
              OutlinedButton(
                  onClick = onAskToBeReplaced,
                  shape = RoundedCornerShape(CornerRadiusHuge),
                  modifier =
                      Modifier.testTag(ReplacementEmployeeListTestTags.ASK_BUTTON)
                          .height(heightLarge)
                          .fillMaxWidth(WeightHeavy)) {
                    Text(text = stringResource(R.string.replacement_ask_to_be_replaced))
                  }
            }
      }) { inner ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = PaddingLarge)
                    .testTag(ReplacementEmployeeListTestTags.ROOT),
            verticalArrangement = Arrangement.spacedBy(PaddingLarge)) {
              requests.forEach { req ->
                ReplacementRequestCard(
                    data = req,
                    onAccept = { onAccept(req.id) },
                    onRefuse = { onRefuse(req.id) },
                    testTag = ReplacementEmployeeListTestTags.card(req.id),
                    acceptTag = ReplacementEmployeeListTestTags.accept(req.id),
                    refuseTag = ReplacementEmployeeListTestTags.refuse(req.id))
              }
              Spacer(Modifier.height(heightLarge))
            }
      }
}

/**
 * **ReplacementRequestCard**
 *
 * Displays a single replacement request entry in the employee replacement list. Each card shows the
 * event’s **date**, **time**, **title**, and **description**, and provides two actions — **accept**
 * or **refuse** — for the employee to respond to the request.
 * ---
 * ### UI Structure:
 * - **Header:** Shows weekday/day and time range.
 * - **Body:** Displays event title and description.
 * - **Footer:** Contains two text buttons:
 *     - “Accept” → confirms the replacement.
 *     - “Refuse” → declines the request.
 * ---
 * ### Parameters:
 *
 * @param data The [ReplacementRequestUi] object containing the event information.
 * @param onAccept Called when the user taps the "Accept" button.
 * @param onRefuse Called when the user taps the "Refuse" button.
 * @param testTag The test tag applied to the entire card (for UI testing).
 * @param acceptTag The test tag applied to the "Accept" button.
 * @param refuseTag The test tag applied to the "Refuse" button.
 * @see ReplacementRequestUi
 */
@Composable
private fun ReplacementRequestCard(
    data: ReplacementRequestUi,
    onAccept: () -> Unit,
    onRefuse: () -> Unit,
    testTag: String,
    acceptTag: String,
    refuseTag: String
) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      shape = RoundedCornerShape(CornerRadiusLarge),
      elevation = CardDefaults.cardElevation(defaultElevation = ElevationLow)) {
        Column(modifier = Modifier.padding(PaddingLarge)) {

          // Date and time row
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(PaddingMedium),
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.weekdayAndDay,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = data.timeRange, style = MaterialTheme.typography.titleMedium)
              }

          Spacer(Modifier.height(PaddingMedium))

          // Title
          Text(
              text = stringResource(R.string.replacement_card_title, data.title),
              style = MaterialTheme.typography.bodyLarge,
              maxLines = titleMaxLine,
              overflow = TextOverflow.Ellipsis)

          // Description
          Text(
              text = stringResource(R.string.replacement_card_description, data.description),
              style = MaterialTheme.typography.bodyLarge,
              maxLines = descriptionMaxLine,
              overflow = TextOverflow.Ellipsis)

          Spacer(Modifier.height(PaddingMedium))

          // Accept / Refuse buttons
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

@Preview(showBackground = true)
@Composable
fun ReplacementEmployeeViewScreenPreview() {
  ReplacementEmployeeListScreen()
}
