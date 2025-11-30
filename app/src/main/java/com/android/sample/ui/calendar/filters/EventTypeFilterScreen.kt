package com.android.sample.ui.calendar.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.filters.components.FilterCheckbox
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.*

/**
 * A screen used inside the Filter Bottom Sheet for selecting one or more event types.
 *
 * This composable is shown when the user chooses the **Event Type** category from the main filter
 * menu. It displays:
 * - A header with a back button and title.
 * - A scrollable list of event types, each represented by a [FilterCheckbox].
 * - Bottom action buttons: **Clear** (resets selections) and **Apply** (confirms selections).
 *
 * ### State Management
 * The screen internally manages its own temporary selection state based on the initial [selected]
 * list. When:
 * - **Apply** is pressed → `onApply` is called with the updated list.
 * - **Back** is pressed → returns to the main filter screen without applying changes.
 *
 * ### Test Tags
 * For UI testing, the screen exposes:
 * - A root tag: `Filter_EventType_Screen`
 * - Header tags: back button, title
 * - List tag: `Filter_EventType_List`
 * - Each item tag: `"Filter_EventType_Item_<TypeName>"`
 * - Bottom button tags: `Filter_EventType_Clear` and `Filter_EventType_Apply`
 *
 * These tags allow fine-grained interaction in Compose UI tests.
 *
 * @param selected The list of event types that are initially selected (controlled externally).
 * @param onBack Callback invoked when the user returns to the previous screen without applying.
 * @param onApply Callback invoked when the user confirms their selections.
 */
@Composable
fun EventTypeFilterScreen(
    selected: List<String>,
    onBack: () -> Unit,
    onApply: (List<String>) -> Unit
) {
  // State to hold current selections
  var selections by remember { mutableStateOf(selected.toList()) }

  // Placeholder event types
  val eventTypes =
      listOf(
          "Course",
          "Workshop",
          "Seminar",
          "Conference",
          "Training",
          "Meeting",
          "Lecture",
          "Webinar",
          "Lab Session",
          "Presentation",
          "Office Hours",
          "Hackathon",
          "Networking Event",
          "Panel Discussion",
          "Tutorial",
          "Exam",
          "Review Session",
          "Team Building",
          "Brainstorming",
          "Guest Talk")

  Column(
      modifier = Modifier.padding(PaddingLarge).testTag(FilterScreenTestTags.EVENT_TYPE_SCREEN)) {

        // ----- Header -----
        Row(
            modifier = Modifier.fillMaxWidth().testTag(FilterScreenTestTags.EVENT_TYPE_HEADER),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                  onClick = onBack,
                  modifier = Modifier.testTag(FilterScreenTestTags.EVENT_TYPE_BACK_BUTTON)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.goBack))
                  }

              Text(
                  stringResource(R.string.eventType),
                  style = MaterialTheme.typography.titleLarge,
                  modifier = Modifier.testTag(FilterScreenTestTags.EVENT_TYPE_TITLE))

              Spacer(modifier = Modifier.width(widthSmall))
            }

        Spacer(Modifier.height(SpacingMedium))

        // ----- Scrollable list -----
        LazyColumn(
            modifier = Modifier.weight(Weight).testTag(FilterScreenTestTags.EVENT_TYPE_LIST)) {
              items(eventTypes) { type ->
                Column(
                    modifier =
                        Modifier.testTag(FilterScreenTestTags.EVENT_TYPE_ITEM_PREFIX + type)) {
                      FilterCheckbox(
                          label = type,
                          key = type,
                          isChecked = type in selections,
                          onCheckedChange = { checked ->
                            selections = if (checked) selections + type else selections - type
                          })
                    }

                Spacer(Modifier.height(SpacingSmall))
              }
            }

        Spacer(Modifier.height(SpacingExtraLarge))

        // ----- Bottom Buttons -----
        BottomNavigationButtons(
            onBack = { selections = emptyList() },
            onNext = { onApply(selections) },
            canGoBack = true,
            canGoNext = true,
            backButtonText = stringResource(R.string.clear_all),
            nextButtonText = stringResource(R.string.apply),
            backButtonTestTag = FilterScreenTestTags.EVENT_TYPE_CLEAR_BUTTON,
            nextButtonTestTag = FilterScreenTestTags.EVENT_TYPE_APPLY_BUTTON)
      }
}

@Preview(showBackground = true)
@Composable
fun EventTypeFilterScreenPreview() {
  EventTypeFilterScreen(selected = listOf("Course"), onBack = {}, onApply = {})
}
