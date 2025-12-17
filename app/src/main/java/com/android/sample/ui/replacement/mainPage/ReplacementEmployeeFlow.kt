package com.android.sample.ui.replacement.mainPage

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.replacement.components.SelectDateRangeScreen
import com.android.sample.ui.replacement.components.SelectEventScreen
import java.time.LocalDate
import java.time.ZoneId

// Assisted by AI

/** Entry point for the **Employee Replacement Flow**. */
@Composable
fun ReplacementEmployeeFlow(
    onOrganizeClick: () -> Unit,
    onWaitingConfirmationClick: () -> Unit,
    onConfirmedClick: () -> Unit,
    viewModel: ReplacementEmployeeViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()

  val context = LocalContext.current

  LaunchedEffect(uiState.lastAction) {
    when (uiState.lastAction) {
      ReplacementEmployeeLastAction.ACCEPTED ->
          Toast.makeText(
                  context,
                  context.getString(R.string.replacement_accept_success),
                  Toast.LENGTH_SHORT,
              )
              .show()
      ReplacementEmployeeLastAction.REFUSED ->
          Toast.makeText(
                  context,
                  context.getString(R.string.replacement_refuse_success),
                  Toast.LENGTH_SHORT,
              )
              .show()
      null -> Unit
    }

    uiState.lastAction?.let { viewModel.clearLastAction() }
  }

  when (uiState.step) {
    ReplacementEmployeeStep.LIST,
    ReplacementEmployeeStep.CREATE_OPTIONS -> {
      ReplacementEmployeeListScreen(
          requests =
              uiState.incomingRequests
                  .filter { it.status == ReplacementStatus.WaitingForAnswer }
                  .map { it.toUi(uiState.allUser) },
          callbacks =
              ReplacementEmployeeCallbacks(
                  onAccept = { id -> viewModel.acceptRequest(id) },
                  onRefuse = { id -> viewModel.refuseRequest(id) },
              ),
          isAdmin = uiState.isAdmin,
          adminActions =
              ReplacementAdminActions(
                  onOrganizeClick = onOrganizeClick,
                  onWaitingConfirmationClick = onWaitingConfirmationClick,
                  onConfirmedClick = onConfirmedClick,
              ),
          createRequestActions =
              ReplacementCreateRequestActions(
                  onSelectEvent = { viewModel.goToSelectEvent() },
                  onChooseDateRange = { viewModel.goToSelectDateRange() },
              ),
          processingRequestIds = uiState.processingRequestIds,
      )
    }
    ReplacementEmployeeStep.SELECT_EVENT -> {
      SelectEventScreen(
          onNext = { viewModel.confirmSelectedEventAndCreateReplacement() },
          onBack = { viewModel.backToList() },
          title = stringResource(R.string.replacement_list_title),
          instruction = stringResource(R.string.replacement_list_instruction),
          canGoNext = uiState.selectedEventId != null,
          onEventClick = { event -> viewModel.setSelectedEvent(event.id) },
      )
    }
    ReplacementEmployeeStep.SELECT_DATE_RANGE -> {
      val start = uiState.startDate
      val end = uiState.endDate
      val today = LocalDate.now()

      val hasBothDates = start != null && end != null

      val hasPastDate =
          (start != null && start.isBefore(today)) || (end != null && end.isBefore(today))

      val hasOrderError = start != null && end != null && end.isBefore(start)

      val isValidRange = hasBothDates && !hasPastDate && !hasOrderError

      val errorMessage =
          when {
            !hasBothDates -> null
            hasPastDate -> stringResource(R.string.invalid_date_range_past_message)
            hasOrderError -> stringResource(R.string.invalid_date_range_message)
            else -> null
          }

      SelectDateRangeScreen(
          onNext = {
            if (isValidRange) {
              viewModel.confirmDateRangeAndCreateReplacements()
            }
          },
          onBack = { viewModel.backToList() },
          title = stringResource(R.string.replacement_create_choose_date_range),
          instruction = stringResource(R.string.select_date_range_instruction),
          onStartDateSelected = { viewModel.setStartDate(it) },
          onEndDateSelected = { viewModel.setEndDate(it) },
          canGoNext = isValidRange,
          errorMessage = errorMessage,
      )
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
fun Replacement.toUi(list: List<User>? = null): ReplacementRequestUi {
  val start = event.startDate.atZone(ZoneId.systemDefault())
  val end = event.endDate.atZone(ZoneId.systemDefault())

  // Simple formatting; can be improved later
  val dateLabel = start.toLocalDate().toString()
  val timeRange = "${start.toLocalTime()} - ${end.toLocalTime()}"
  val user = list?.firstOrNull { it.id == absentUserId }?.display() ?: absentUserId

  return ReplacementRequestUi(
      id = id,
      weekdayAndDay = dateLabel,
      timeRange = timeRange,
      title = event.title,
      description = event.description,
      absentDisplayName = user,
      color = event.category.color)
}
