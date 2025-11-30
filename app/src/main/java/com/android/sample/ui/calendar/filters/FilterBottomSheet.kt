package com.android.sample.ui.calendar.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.sample.R
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium

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
  const val TITLE = "FilterSheet_Title"
  const val CLOSE_BUTTON = "FilterSheet_CloseButton"

  // Category items
  const val CATEGORY_EVENT_TYPE = "Filter_EventType"
  const val CATEGORY_LOCATION = "Filter_Location"
  const val CATEGORY_PARTICIPANTS = "Filter_Participants"

  // Buttons
  const val BUTTON_ROW = "FilterSheet_ButtonRow"
  const val CLEAR_ALL = "FilterSheet_ClearAll"
  const val APPLY = "FilterSheet_Apply"

  // Event Type Filter Screen
  const val EVENT_TYPE_SCREEN = "Filter_EventType_Screen"
  const val EVENT_TYPE_HEADER = "Filter_EventType_Header"
  const val EVENT_TYPE_BACK_BUTTON = "Filter_EventType_Back"
  const val EVENT_TYPE_TITLE = "Filter_EventType_Title"

  const val EVENT_TYPE_LIST = "Filter_EventType_List"
  const val EVENT_TYPE_ITEM_PREFIX = "Filter_EventType_Item_" // + type name

  const val EVENT_TYPE_CLEAR_BUTTON = "Filter_EventType_Clear"
  const val EVENT_TYPE_APPLY_BUTTON = "Filter_EventType_Apply"
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
fun FilterBottomSheet(onDismiss: () -> Unit, onApply: (Map<String, List<String>>) -> Unit) {
  // States for all filters
  var eventTypeFilters by remember { mutableStateOf(listOf<String>()) }
  var locationFilters by remember { mutableStateOf(listOf<String>()) }
  var participantFilters by remember { mutableStateOf(listOf<String>()) }

  // Page state (MAIN / EVENT_TYPE / LOCATION / PARTICIPANTS)
  var currentPage by remember { mutableStateOf(FilterPage.MAIN) }

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
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.filter),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.testTag(FilterScreenTestTags.TITLE))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag(FilterScreenTestTags.CLOSE_BUTTON)) {
                              Icon(
                                  imageVector = Icons.Default.Close,
                                  contentDescription = stringResource(R.string.close),
                                  tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                      }

                  Spacer(Modifier.height(SpacingLarge))

                  // ====== Category Block ======
                  Column(
                      modifier =
                          Modifier.fillMaxWidth()
                              .clip(RoundedCornerShape(CornerRadiusLarge))
                              .background(
                                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AlphaLow))
                              .padding(vertical = PaddingSmall)) {

                        // ----- Category: Event Type -----
                        FilterCategoryItem(
                            name = stringResource(R.string.filter_event_type),
                            count = eventTypeFilters.size,
                            tag = FilterScreenTestTags.CATEGORY_EVENT_TYPE,
                            onClick = { currentPage = FilterPage.EVENT_TYPE })

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AlphaLow))

                        // ----- Category: Location -----
                        FilterCategoryItem(
                            name = stringResource(R.string.filter_location),
                            count = locationFilters.size,
                            tag = FilterScreenTestTags.CATEGORY_LOCATION,
                            onClick = { currentPage = FilterPage.LOCATION })

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AlphaLow))

                        // ----- Category: Participants -----
                        FilterCategoryItem(
                            name = stringResource(R.string.filter_participants),
                            count = participantFilters.size,
                            tag = FilterScreenTestTags.CATEGORY_PARTICIPANTS,
                            onClick = { currentPage = FilterPage.PARTICIPANTS })
                      }

                  Spacer(Modifier.height(SpacingExtraLarge))

                  BottomNavigationButtons(
                      onBack = {
                        eventTypeFilters = emptyList()
                        locationFilters = emptyList()
                        participantFilters = emptyList()
                      },
                      onNext = {
                        onApply(
                            mapOf(
                                "types" to eventTypeFilters,
                                "locations" to locationFilters,
                                "participants" to participantFilters))
                      },
                      canGoBack = true,
                      canGoNext = true,
                      backButtonText = stringResource(R.string.clear_all),
                      nextButtonText = stringResource(R.string.apply),
                      backButtonTestTag = FilterScreenTestTags.CLEAR_ALL,
                      nextButtonTestTag = FilterScreenTestTags.APPLY)

                  Spacer(Modifier.height(SpacingMedium))
                }
          }
          // -------------------------------
          // EVENT TYPE FILTER SCREEN
          // -------------------------------
          FilterPage.EVENT_TYPE -> {
            EventTypeFilterScreen(
                selected = eventTypeFilters,
                onBack = { currentPage = FilterPage.MAIN },
                onApply = {
                  eventTypeFilters = it
                  currentPage = FilterPage.MAIN
                })
          }

          // -------------------------------
          // LOCATION FILTER SCREEN
          // -------------------------------
          FilterPage.LOCATION -> {
            LocationFilterScreen(
                selected = locationFilters,
                onBack = { currentPage = FilterPage.MAIN },
                onApply = {
                  locationFilters = it
                  currentPage = FilterPage.MAIN
                })
          }

          // -------------------------------
          // PARTICIPANTS FILTER SCREEN
          // -------------------------------
          FilterPage.PARTICIPANTS -> {
            ParticipantFilterScreen(
                selected = participantFilters,
                onBack = { currentPage = FilterPage.MAIN },
                onApply = {
                  participantFilters = it
                  currentPage = FilterPage.MAIN
                })
          }
        }
      }
}

/** Single filter category row. */
@Composable
private fun FilterCategoryItem(name: String, count: Int, tag: String, onClick: () -> Unit) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = PaddingMedium)
              .clickable(onClick = onClick)
              .testTag(tag),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag("${tag}_Label"))

        Text(
            text = if (count > 0) "($count)" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("${tag}_Count"))
      }
}
