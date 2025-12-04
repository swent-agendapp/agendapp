package com.android.sample.ui.replacement.organize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.replacement.components.ReplacementProcessActions
import com.android.sample.ui.replacement.components.SelectDateRangeScreen
import com.android.sample.ui.replacement.components.SelectEventScreen
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
  const val DATE_RANGE_INVALID_TEXT = "date_range_invalid_text"
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
    onProcessNow: (Replacement) -> Unit = {},
    onProcessLater: () -> Unit = {},
    replacementOrganizeViewModel: ReplacementOrganizeViewModel = viewModel()
) {
  LaunchedEffect(Unit) { replacementOrganizeViewModel.loadOrganizationMembers() }
  val uiState by replacementOrganizeViewModel.uiState.collectAsState()

  val memberLabel =
      uiState.selectedMember?.displayName
          ?: uiState.selectedMember?.email
          ?: uiState.selectedMember?.id

  when (uiState.step) {
    ReplacementOrganizeStep.SelectSubstitute ->
        SelectSubstitutedScreen(
            onBack = onCancel,
            onSelectEvents = {
              replacementOrganizeViewModel.goToStep(ReplacementOrganizeStep.SelectEvents)
            },
            onSelectDateRange = {
              replacementOrganizeViewModel.goToStep(ReplacementOrganizeStep.SelectDateRange)
            },
            replacementOrganizeViewModel = replacementOrganizeViewModel)
    ReplacementOrganizeStep.SelectEvents ->
        SelectEventScreen(
            onNext = {},
            onBack = {
              replacementOrganizeViewModel.goToStep(ReplacementOrganizeStep.SelectSubstitute)
            },
            title = stringResource(R.string.organize_replacement),
            instruction = stringResource(R.string.select_replacement_events, memberLabel ?: ""),
            onEventClick = { event -> replacementOrganizeViewModel.toggleSelectedEvent(event) },
            canGoNext = uiState.selectedEvents.isNotEmpty(),
            processActions =
                ReplacementProcessActions(
                    onProcessNow = {
                      replacementOrganizeViewModel.addReplacement(
                          status = ReplacementStatus.ToProcess,
                          onReplacementsCreated = { replacements ->
                            val first = replacements.firstOrNull() ?: return@addReplacement
                            onProcessNow(first)
                          },
                      )
                    },
                    onProcessLater = {
                      replacementOrganizeViewModel.addReplacement(
                          status = ReplacementStatus.ToProcess,
                          onReplacementsCreated = { onProcessLater() },
                      )
                    },
                ))
    ReplacementOrganizeStep.SelectDateRange ->
        SelectDateRangeScreen(
            onNext = {},
            onBack = {
              replacementOrganizeViewModel.goToStep(ReplacementOrganizeStep.SelectSubstitute)
            },
            initialStartInstant = uiState.startInstant,
            initialEndInstant = uiState.endInstant,
            onStartDateSelected = {
              replacementOrganizeViewModel.setStartInstant(
                  DateTimeUtils.instantWithDate(instant = uiState.startInstant, date = it))
            },
            onEndDateSelected = {
              replacementOrganizeViewModel.setEndInstant(
                  DateTimeUtils.instantWithDate(instant = uiState.endInstant, date = it))
            },
            title = stringResource(R.string.organize_replacement),
            instruction =
                stringResource(
                    R.string.select_replacement_date_range,
                    memberLabel ?: "",
                ),
            errorMessage = stringResource(R.string.invalidDateRangeMessage),
            canGoNext = replacementOrganizeViewModel.dateRangeValid(),
            onProcessNow = {
              replacementOrganizeViewModel.addReplacement(
                  status = ReplacementStatus.ToProcess,
                  onReplacementsCreated = { replacements ->
                    val first = replacements.firstOrNull() ?: return@addReplacement
                    onProcessNow(first)
                  },
              )
            },
            onProcessLater = {
              replacementOrganizeViewModel.addReplacement(
                  status = ReplacementStatus.ToProcess,
                  onReplacementsCreated = { onProcessLater() },
              )
            },
        )
  }
}
