package com.android.sample.ui.calendar.filters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.common.MemberSelectionList
import com.android.sample.ui.common.MemberSelectionListOptions
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.WeightVeryHeavy

// Assisted by AI

/**
 * Test tags used in UI tests for the Filter screen.
 *
 * These tags are attached to composables via `Modifier.testTag(...)` and allow tests to reliably
 * query and interact with UI elements.
 *
 * Each constant corresponds to a specific UI element in the Filter screen.
 */
object FilterScreenTestTags {

  // Content
  const val FILTER_SHEET_CONTENT = "FilterSheet_Content"

  // Header
  const val HEADER = "FilterSheet_Header"

  // Category items
  const val CATEGORY_EVENT_TYPE = "Filter_EventType"
  const val CATEGORY_LOCATION = "Filter_Location"
  const val CATEGORY_PARTICIPANTS = "Filter_Participants"

  // Buttons
  const val BUTTON_ROW = "FilterSheet_ButtonRow"
  const val CLEAR_ALL = "FilterSheet_ClearAll"
  const val APPLY = "FilterSheet_Apply"
}

enum class FilterPage {
  MAIN,
  EVENT_TYPE,
  LOCATION,
  PARTICIPANTS
}

/**
 * A bottom sheet allowing users to filter events in the calendar.
 *
 * @param onDismiss Called when the sheet should close.
 * @param onApply Called when the user applies filters. The current filters are sent as a Map.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(users: List<User>, categories: List<EventCategory>, onDismiss: () -> Unit, onApply: (Map<String, List<String>>) -> Unit) {
  // States for all filters
  var eventTypeFilters by remember { mutableStateOf(listOf<String>()) }
  var locationFilters by remember { mutableStateOf(listOf<String>()) }
  var participantFilters by remember { mutableStateOf(listOf<String>()) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  // Page state (MAIN / EVENT_TYPE / LOCATION / PARTICIPANTS)
  var currentPage by remember { mutableStateOf(FilterPage.MAIN) }

  LaunchedEffect(currentPage) {
    if (currentPage != FilterPage.MAIN) {
      sheetState.expand()
    }
  }
  ModalBottomSheet(
      onDismissRequest = onDismiss,
      modifier = Modifier.testTag(CalendarScreenTestTags.FILTER_BOTTOM_SHEET)) {
        when (currentPage) {

          // -------------------------------
          // MAIN FILTER SCREEN
          // -------------------------------
          FilterPage.MAIN -> {
            Column(
                modifier =
                    Modifier.padding(PaddingLarge)
                        .testTag(FilterScreenTestTags.FILTER_SHEET_CONTENT)) {

                  // ----- Header -----
                  Row(
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(vertical = PaddingSmall)
                              .testTag(FilterScreenTestTags.HEADER),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {}

                  Spacer(Modifier.height(SpacingLarge))

                  // ====== Category Block ======
                  Card(
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(CornerRadiusLarge),
                      colors =
                          CardDefaults.cardColors(
                              containerColor =
                                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AlphaLow)),
                  ) {
                    FilterCategoryItem(
                        title = stringResource(R.string.filter_event_type),
                        count =  eventTypeFilters.size,
                        tag = FilterScreenTestTags.CATEGORY_EVENT_TYPE,
                        onClick = { currentPage = FilterPage.EVENT_TYPE },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AlphaLow))

                    FilterCategoryItem(
                        title = stringResource(R.string.filter_location),
                        count = locationFilters.size,
                        tag = FilterScreenTestTags.CATEGORY_LOCATION,
                        onClick = { currentPage = FilterPage.LOCATION },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AlphaLow))

                    FilterCategoryItem(
                        title = stringResource(R.string.filter_participants),
                        count = participantFilters.size,
                        tag = FilterScreenTestTags.CATEGORY_PARTICIPANTS,
                        onClick = { currentPage = FilterPage.PARTICIPANTS },
                    )
                  }
                }
          }
          // -------------------------------
          // EVENT TYPE FILTER SCREEN
          // -------------------------------
          FilterPage.EVENT_TYPE -> {
              val labelToId = remember(categories) {
                  categories.associateBy(
                      keySelector = { it.label },
                      valueTransform = { it.id }
                  )
              }
              val idToLabel = remember(categories) {
                  categories.associateBy(
                      keySelector = { it.id },
                      valueTransform = { it.label }
                  )
              }

              val categoryLabels = remember(categories) {
                  categories
                      .map { it.label }
                      .distinct()
                      .sortedBy { it.trim().lowercase() }
              }

              val selectedLabels = remember(eventTypeFilters, idToLabel) {
                  eventTypeFilters.mapNotNull { idToLabel[it] }
              }

            FilterListScreen(
                title = stringResource(R.string.eventType),
                items = categoryLabels,
                selected = selectedLabels,
                testTagPrefix = "EventTypeFilter",
                onBack = { currentPage = FilterPage.MAIN },
                onApply = { selectionsLabels ->
                    eventTypeFilters = selectionsLabels.mapNotNull { labelToId[it] }
                  onApply(
                      mapOf(
                          "types" to eventTypeFilters,
                          "locations" to locationFilters,
                          "participants" to participantFilters))
                  currentPage = FilterPage.MAIN
                })
          }

          // -------------------------------
          // LOCATION FILTER SCREEN
          // -------------------------------
          FilterPage.LOCATION -> {
            val locations = listOf("Salle 1", "Salle 2", "Unknown")

            FilterListScreen(
                title = stringResource(R.string.location),
                items = locations,
                selected = locationFilters,
                testTagPrefix = "LocationFilter",
                onBack = { currentPage = FilterPage.MAIN },
                onApply = { selections ->
                  locationFilters = selections

                  onApply(
                      mapOf(
                          "types" to eventTypeFilters,
                          "locations" to locationFilters,
                          "participants" to participantFilters))

                  currentPage = FilterPage.MAIN
                })
          }

          // -------------------------------
          // PARTICIPANTS FILTER SCREEN
          // -------------------------------
          FilterPage.PARTICIPANTS -> {
              val labelToId = remember(users) {
                  users.associateBy(
                      keySelector = { it.display() },
                      valueTransform = { it.id }
                  )
              }
              val idToLabel = remember(users) {
                  users.associateBy(
                      keySelector = { it.id },
                      valueTransform = { it.display() }
                  )
              }
              val participantLabels = remember(users) {
                  users
                      .map { it.display() }
                      .distinct()
                      .sortedBy { it.trim().lowercase() }
              }

              val selectedLabels = remember(participantFilters, idToLabel) {
                  participantFilters.mapNotNull { idToLabel[it] }
              }

            FilterListScreen(
                title = stringResource(R.string.filter_participants),
                items = participantLabels,
                selected = selectedLabels,
                testTagPrefix = "ParticipantFilter",
                onBack = { currentPage = FilterPage.MAIN },
                onApply = { selectionsLabels ->
                    participantFilters = selectionsLabels.mapNotNull { labelToId[it] }

                  onApply(
                      mapOf(
                          "types" to eventTypeFilters,
                          "locations" to locationFilters,
                          "participants" to participantFilters))

                  currentPage = FilterPage.MAIN
                })
          }
        }
      }
}

/** Single filter category row. */
@Composable
private fun FilterCategoryItem(title: String, count: Int, tag: String, onClick: () -> Unit) {
  ListItem(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(tag),
      headlineContent = {
        Text(
            text = title,
            modifier = Modifier.testTag("${tag}_Label"),
            style = MaterialTheme.typography.bodyLarge)
      },
      supportingContent = {
        if (count > 0) {
          Text(
              text = "$count selected",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              style = MaterialTheme.typography.bodyMedium)
        }
      },
      trailingContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (count > 0) {
            Text(
                text = "($count)",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("${tag}_Count"),
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(PaddingSmall))
          }
          Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      })
}
