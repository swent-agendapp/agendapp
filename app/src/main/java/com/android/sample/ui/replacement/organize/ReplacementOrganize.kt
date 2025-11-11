package com.android.sample.ui.replacement.organize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.ui.replacement.components.SelectDateRangeScreen
import com.android.sample.ui.replacement.components.SelectEventScreen
import com.android.sample.ui.replacement.organize.components.SelectProcessMomentScreen
import com.android.sample.ui.replacement.organize.components.SelectSubstitutedScreen

/** Contains the test tags used across the replacement organization screen UI. */
object ReplacementOrganizeTestTags {
  const val INSTRUCTION_TEXT = "instruction_text"
  const val SEARCH_BAR = "search_bar"
  const val MEMBER_LIST = "member_list"
  const val SELECTED_MEMBER_INFO = "selected_member_info"
  const val SELECT_EVENT_BUTTON = "select_event_button"
  const val SELECT_DATE_RANGE_BUTTON = "select_date_range_button"
  const val START_DATE_FIELD = "start_date_field"
  const val END_DATE_FIELD = "end_date_field"
  const val PROCESS_NOW_BUTTON = "process_now_button"
  const val PROCESS_LATER_BUTTON = "process_later_button"
  const val NEXT_BUTTON = "next_button"
  const val BACK_BUTTON = "back_button"
}

/**
 * Entry-point composable for the **Organize Replacement** feature.
 *
 * This composable manages the multi-step workflow involved in organizing a replacement for an
 * absent member. It internally manages the navigation logic with a view model. Individual screens
 * do not navigate directly; instead they request navigation by invoking callbacks.
 *
 * @param onCancel Callback triggered when the user cancels the flow
 */
@Composable
fun ReplacementOrganizeScreen(
    onCancel: () -> Unit = {},
    onProcessNow: () -> Unit = {},
    onProcessLater: () -> Unit = {}
) {

  // This currentStep be handled by the view model in a complete implementation
  var currentStep by remember { mutableIntStateOf(1) }

  when (currentStep) {
    1 ->
        SelectSubstitutedScreen(
            onBack = onCancel,
            onSelectEvents = { currentStep = 2 },
            onSelectDateRange = { currentStep = 3 })
    2 ->
        SelectEventScreen(
            onNext = { currentStep = 4 },
            onBack = { currentStep = 1 },
            instruction = stringResource(R.string.select_replacement_events))
    3 ->
        SelectDateRangeScreen(
            onNext = { currentStep = 4 },
            onBack = { currentStep = 1 },
            instruction = stringResource(R.string.organize_replacement))
    4 -> SelectProcessMomentScreen(onProcessNow = onProcessNow, onProcessLater = onProcessLater)
  }
}
