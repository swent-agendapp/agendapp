package com.android.sample.ui.calendar.replacementEmployee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementCreateScreen
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementEmployeeListScreen
import com.android.sample.ui.calendar.replacementEmployee.components.ReplacementRequestUi
import com.android.sample.ui.replacement.components.SelectDateRangeScreen
import com.android.sample.ui.replacement.components.SelectEventScreen
import java.time.ZoneId

// Assisted by AI

/**
 * Entry point for the **Employee Replacement Flow**.
 *
 * It orchestrates which screen to show based on [ReplacementEmployeeUiState.step]:
 * - LIST → [ReplacementEmployeeListScreen]
 * - CREATE_OPTIONS → [ReplacementCreateScreen]
 * - SELECT_EVENT → [SelectEventScreen] (calendar event selection)
 * - SELECT_DATE_RANGE → [SelectDateRangeScreen] (date interval)
 */
@Composable
fun ReplacementEmployeeFlow(viewModel: ReplacementEmployeeViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()

  when (uiState.step) {
    ReplacementEmployeeStep.LIST -> {
      ReplacementEmployeeListScreen(
          requests = uiState.incomingRequests.map { it.toUi() },
          onAccept = { id -> viewModel.acceptRequest(id) },
          onRefuse = { id -> viewModel.refuseRequest(id) },
          onAskToBeReplaced = { viewModel.goToCreateOptions() })
    }
    ReplacementEmployeeStep.CREATE_OPTIONS -> {
      ReplacementCreateScreen(
          onSelectEvent = { viewModel.goToSelectEvent() },
          onChooseDateRange = { viewModel.goToSelectDateRange() },
          onBack = { viewModel.backToList() })
    }
    ReplacementEmployeeStep.SELECT_EVENT -> {
      // NOTE: actual event selection calendar will be implemented by another teammate.
      // For now, we only pass title/instruction and the "Next" enable state.
      SelectEventScreen(
          onNext = { viewModel.confirmSelectedEventAndCreateReplacement() },
          onBack = { viewModel.backToCreateOptions() },
          title = stringResource(R.string.replacement_list_title),
          instruction = stringResource(R.string.replacement_list_instruction),
          canGoNext = uiState.selectedEventId != null)
    }
    ReplacementEmployeeStep.SELECT_DATE_RANGE -> {
      SelectDateRangeScreen(
          onNext = { viewModel.confirmDateRangeAndCreateReplacements() },
          onBack = { viewModel.backToCreateOptions() },
          title = stringResource(R.string.replacement_create_choose_date_range),
          instruction = stringResource(R.string.select_date_range_instruction),
          onStartDateSelected = { viewModel.setStartDate(it) },
          onEndDateSelected = { viewModel.setEndDate(it) })
    }
  }
}

/**
 * Maps a domain [Replacement] model to the UI-friendly [ReplacementRequestUi] used by
 * [ReplacementEmployeeListScreen].
 *
 * Date/time formatting is currently kept simple to avoid dependency on date utils. You can later
 * replace the `toString()` calls with proper formatting using `DateTimeUtils`.
 */
fun Replacement.toUi(): ReplacementRequestUi {
  val start = event.startDate.atZone(ZoneId.systemDefault())
  val end = event.endDate.atZone(ZoneId.systemDefault())

  // Simple formatting; can be improved later
  val dateLabel = start.toLocalDate().toString()
  val timeRange = "${start.toLocalTime()} - ${end.toLocalTime()}"

  return ReplacementRequestUi(
      id = id,
      weekdayAndDay = dateLabel,
      timeRange = timeRange,
      title = event.title,
      description = event.description)
}
