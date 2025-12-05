package com.android.sample.ui.calendar

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.components.DatePickerModal
import com.android.sample.ui.calendar.components.ViewMode
import com.android.sample.ui.calendar.components.ViewModeSelector
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.utils.DateTimeUtils.localDateTimeToInstant
import com.android.sample.ui.theme.PaddingExtraSmall
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

// Modularization assisted by AI

/**
 * High-level container for the calendar screen. It hosts the grid background, the events layer, and
 * view mode selection.
 *
 * This composable owns the visible [LocalDateRange], handles week / day navigation on horizontal
 * swipes, and triggers loading of events from [CalendarViewModel] whenever the range changes.
 *
 * @param modifier [Modifier] applied to the root container.
 * @param calendarViewModel ViewModel used to fetch events for the visible date range.
 * @param onEventClick Callback invoked when the user taps on an [Event].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContainer(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = viewModel(),
    onEventClick: (Event) -> Unit = {}
) {
  // "Today" is used to compute the initial view mode and initial date range.
  val today = remember { LocalDate.now() }

  // Default mode: 5 days if today is Monday–Friday, otherwise 7 days (Saturday / Sunday).
  val defaultViewMode = remember { computeDefaultViewMode(today) }

  // Current mode currently displayed.
  var currentMode by remember { mutableStateOf(defaultViewMode) }

  // Last non-month mode (ONE_DAY, FIVE_DAYS, or SEVEN_DAYS).
  // This is used when returning from the MONTH mode.
  var previousNonMonthMode by remember { mutableStateOf(defaultViewMode) }

  // Visible date range in the grid (day / week).
  var currentDateRange by remember {
    mutableStateOf(computeInitialDateRange(today, defaultViewMode))
  }

  // Controls whether the month picker dialog is visible.
  var showMonthPicker by remember { mutableStateOf(false) }

  // Observe events from the view model.
  val uiState by calendarViewModel.uiState.collectAsState()
  val events = uiState.events

  // Fetch events whenever the visible date range changes.
  LaunchedEffect(currentDateRange) { loadEventsForDateRange(calendarViewModel, currentDateRange) }

  // Header shows the same range as the grid, except in ONE_DAY mode.
  val headerDateRange =
      remember(currentDateRange, currentMode) {
        computeHeaderDateRange(currentDateRange, currentMode)
      }

  // In ONE_DAY mode, this is the day currently displayed in the grid.
  // It will be highlighted as "selected" in the header.
  val selectedDate =
      remember(currentDateRange, currentMode) { computeSelectedDate(currentDateRange, currentMode) }

  Box(modifier = modifier) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = {
          calendarViewModel.refreshEvents(
              localDateTimeToInstant(currentDateRange.start, LocalTime.MIDNIGHT),
              localDateTimeToInstant(currentDateRange.endInclusive, LocalTime.MAX))
        },
        modifier = Modifier.testTag(CalendarScreenTestTags.PULL_TO_REFRESH)) {
          // When the mode is not MONTH, we show the week/day grid.
          if (currentMode != ViewMode.MONTH) {
            CalendarGridContent(
                modifier =
                    Modifier.fillMaxSize()
                        .testTag(CalendarScreenTestTags.ROOT)
                        // Handle horizontal swipes to navigate between days/weeks.
                        .calendarSwipeGestures(
                            currentDateRange = currentDateRange,
                            currentMode = currentMode,
                            onRangeChanged = { newRange -> currentDateRange = newRange }),
                dateRange = currentDateRange,
                headerDateRange = headerDateRange,
                events = events,
                today = today,
                selectedDate = selectedDate,
                // Header is clickable only in ONE_DAY mode.
                onHeaderDayClick =
                    buildOnHeaderDayClick(currentMode) { clickedDate ->
                      // When the user clicks a day in the header while in ONE_DAY mode,
                      // we update the visible range to show that single day.
                      currentDateRange = LocalDateRange(clickedDate, clickedDate)
                    },
                onEventClick = onEventClick)
          }
        }

    // Floating button to change the view mode.
    ViewModeSelector(
        modifier =
            Modifier.align(Alignment.TopStart)
                .padding(start = PaddingExtraSmall, top = PaddingExtraSmall),
        currentMode = currentMode,
        onModeSelected = { newMode ->
          // Delegate the complex mode change logic to a helper.
          val result =
              handleViewModeSelection(
                  newMode = newMode,
                  today = today,
                  currentMode = currentMode,
                  currentRange = currentDateRange,
                  previousNonMonthMode = previousNonMonthMode)

          currentMode = result.updatedMode
          previousNonMonthMode = result.updatedPreviousNonMonthMode
          currentDateRange = result.updatedRange
          showMonthPicker = result.shouldShowMonthPicker
        })

    // MONTH mode: show the DatePickerModal instead of the grid.
    if (currentMode == ViewMode.MONTH && showMonthPicker) {
      Box(modifier = Modifier.testTag(CalendarScreenTestTags.DATE_PICKER_MODAL)) {
        DatePickerModal(
            onDateSelected = { selectedMillis ->
              // Delegate the date selection logic from the month picker to a helper.
              val result =
                  handleMonthDateSelected(
                      selectedMillis = selectedMillis,
                      previousNonMonthMode = previousNonMonthMode,
                      currentRange = currentDateRange)

              currentMode = result.updatedMode
              currentDateRange = result.updatedRange
              showMonthPicker = result.shouldShowMonthPicker
              previousNonMonthMode = result.updatedPreviousNonMonthMode
            },
            onDismiss = {
              // If the dialog is dismissed, we return to the last non-month mode and keep the
              // previous date range.
              val result =
                  handleMonthPickerDismiss(
                      previousNonMonthMode = previousNonMonthMode, currentRange = currentDateRange)

              currentMode = result.updatedMode
              currentDateRange = result.updatedRange
              showMonthPicker = result.shouldShowMonthPicker
              previousNonMonthMode = result.updatedPreviousNonMonthMode
            })
      }
    }
  }
}

/**
 * Loads the calendar events for a given date range using the provided [CalendarViewModel].
 *
 * Converts the [LocalDateRange] into corresponding [java.time.Instant] values covering the full
 * duration from start of the first day (midnight) to the end of the last day.
 */
fun loadEventsForDateRange(calendarViewModel: CalendarViewModel, dateRange: LocalDateRange) {
  calendarViewModel.loadEventsBetween(
      localDateTimeToInstant(dateRange.start, LocalTime.MIDNIGHT),
      localDateTimeToInstant(dateRange.endInclusive, LocalTime.MAX))
}

/**
 * Returns the default view mode:
 * - FIVE_DAYS if today is Monday–Friday
 * - SEVEN_DAYS if today is Saturday or Sunday
 */
fun computeDefaultViewMode(today: LocalDate): ViewMode {
  return if (today.dayOfWeek.value in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value) {
    ViewMode.FIVE_DAYS
  } else {
    ViewMode.SEVEN_DAYS
  }
}

/**
 * Computes the initial date range for the given mode based on "today".
 * - FIVE_DAYS: Monday–Friday of the week containing today
 * - SEVEN_DAYS: Monday–Sunday of the week containing today
 */
fun computeInitialDateRange(today: LocalDate, mode: ViewMode): LocalDateRange {
  return when (mode) {
    ViewMode.FIVE_DAYS -> weekRangeContaining(today, days = 5)
    ViewMode.SEVEN_DAYS -> weekRangeContaining(today, days = 7)
    ViewMode.ONE_DAY -> LocalDateRange(today, today)
    ViewMode.MONTH -> weekRangeContaining(today, days = 7) // Not used directly in MONTH mode.
  }
}

/**
 * Builds a week range starting on Monday for the week containing [date].
 *
 * @param date Any date inside the desired week.
 * @param days Number of visible days (5 or 7).
 */
fun weekRangeContaining(date: LocalDate, days: Int): LocalDateRange {
  val monday = date.with(DayOfWeek.MONDAY)
  val end = monday.plusDays((days - 1).toLong())
  return LocalDateRange(monday, end)
}

/**
 * Computes the header date range.
 *
 * In ONE_DAY mode, the header shows the full week containing the visible day. In other modes, the
 * header uses the same range as the grid.
 */
fun computeHeaderDateRange(
    currentDateRange: LocalDateRange,
    currentMode: ViewMode
): LocalDateRange {
  return if (currentMode == ViewMode.ONE_DAY) {
    weekRangeContaining(currentDateRange.start, days = 7)
  } else {
    currentDateRange
  }
}

/**
 * Computes the selected date in the header.
 *
 * In ONE_DAY mode, the selected date is the only visible day. In other modes, there is no specific
 * selected date.
 */
fun computeSelectedDate(currentDateRange: LocalDateRange, currentMode: ViewMode): LocalDate? {
  return if (currentMode == ViewMode.ONE_DAY) currentDateRange.start else null
}

/**
 * Updates the date range when the user swipes horizontally.
 * - In ONE_DAY mode: moves by 1 day forward/backward.
 * - In FIVE_DAYS or SEVEN_DAYS: moves by one whole week forward/backward (starting on Monday).
 */
fun updateDateRangeForSwipe(
    currentRange: LocalDateRange,
    currentMode: ViewMode,
    moveToPrevious: Boolean,
): LocalDateRange {
  val sign = if (moveToPrevious) -1L else 1L

  return when (currentMode) {
    ViewMode.ONE_DAY -> {
      val newDay = currentRange.start.plusDays(sign)
      LocalDateRange(newDay, newDay)
    }
    ViewMode.FIVE_DAYS -> {
      val anchor = currentRange.start.plusWeeks(sign)
      weekRangeContaining(anchor, days = 5)
    }
    ViewMode.SEVEN_DAYS,
    ViewMode.MONTH -> {
      // MONTH should not normally be swipable, but we fallback to a 7-day week logic.
      val anchor = currentRange.start.plusWeeks(sign)
      weekRangeContaining(anchor, days = 7)
    }
  }
}

/**
 * Updates the date range when the user changes the view mode via [ViewModeSelector].
 *
 * Rules:
 * - When switching from FIVE_DAYS or SEVEN_DAYS to ONE_DAY:
 *     - If "today" is inside the current range, show today.
 *     - Otherwise, show Monday of the current range.
 * - When switching to FIVE_DAYS or SEVEN_DAYS, we show the week (starting Monday) containing the
 *   current start date.
 * - When switching to MONTH, the range is kept as-is; the real update comes from the date selected
 *   in the [DatePickerModal].
 */
fun updateDateRangeForModeChange(
    previousMode: ViewMode,
    newMode: ViewMode,
    currentRange: LocalDateRange,
    today: LocalDate,
): LocalDateRange {
  return when (newMode) {
    ViewMode.ONE_DAY -> {
      if (previousMode == ViewMode.FIVE_DAYS || previousMode == ViewMode.SEVEN_DAYS) {
        val todayInRange =
            !today.isBefore(currentRange.start) && !today.isAfter(currentRange.endInclusive)
        val targetDay =
            if (todayInRange) {
              today
            } else {
              // If today is not in the current range, fall back to Monday.
              currentRange.start.with(DayOfWeek.MONDAY)
            }
        LocalDateRange(targetDay, targetDay)
      } else {
        // From ONE_DAY or MONTH to ONE_DAY: keep the current start day as the focused day.
        LocalDateRange(currentRange.start, currentRange.start)
      }
    }
    ViewMode.FIVE_DAYS -> weekRangeContaining(currentRange.start, days = 5)
    ViewMode.SEVEN_DAYS -> weekRangeContaining(currentRange.start, days = 7)
    ViewMode.MONTH -> currentRange
  }
}

/**
 * Result of a view mode selection.
 *
 * This groups together all state changes related to a mode change.
 */
data class ViewModeSelectionResult(
    val updatedMode: ViewMode,
    val updatedRange: LocalDateRange,
    val updatedPreviousNonMonthMode: ViewMode,
    val shouldShowMonthPicker: Boolean,
)

/**
 * Handles the logic when the user selects a new [ViewMode] from [ViewModeSelector].
 *
 * This keeps the composable body simple by returning all computed values in a single object.
 */
fun handleViewModeSelection(
    newMode: ViewMode,
    today: LocalDate,
    currentMode: ViewMode,
    currentRange: LocalDateRange,
    previousNonMonthMode: ViewMode,
): ViewModeSelectionResult {
  return when (newMode) {
    ViewMode.MONTH -> {
      // Enter MONTH mode and show the month picker dialog.
      ViewModeSelectionResult(
          updatedMode = ViewMode.MONTH,
          updatedRange = currentRange,
          updatedPreviousNonMonthMode = previousNonMonthMode,
          shouldShowMonthPicker = true)
    }
    else -> {
      // For non-month modes we remember this as the "last used" mode.
      val updatedRange =
          updateDateRangeForModeChange(
              previousMode = currentMode,
              newMode = newMode,
              currentRange = currentRange,
              today = today,
          )
      ViewModeSelectionResult(
          updatedMode = newMode,
          updatedRange = updatedRange,
          updatedPreviousNonMonthMode = newMode,
          shouldShowMonthPicker = false)
    }
  }
}

/**
 * Result of an interaction with the month picker.
 *
 * This is used both when a date is selected and when the dialog is dismissed.
 */
data class MonthPickerResult(
    val updatedMode: ViewMode,
    val updatedRange: LocalDateRange,
    val shouldShowMonthPicker: Boolean,
    val updatedPreviousNonMonthMode: ViewMode,
)

/**
 * Handles a date selection from the month picker.
 * - If no date is selected, we simply return to the previous non-month mode.
 * - If a date is selected, we reopen the corresponding day/week according to the previously used
 *   non-month mode, with a special rule:
 *     - If the previous mode was FIVE_DAYS and the selected date is on weekend, we use SEVEN_DAYS.
 */
fun handleMonthDateSelected(
    selectedMillis: Long?,
    previousNonMonthMode: ViewMode,
    currentRange: LocalDateRange,
): MonthPickerResult {
  // No date selected: simply go back to the previous non-month mode.
  if (selectedMillis == null) {
    return MonthPickerResult(
        updatedMode = previousNonMonthMode,
        updatedRange = currentRange,
        shouldShowMonthPicker = false,
        updatedPreviousNonMonthMode = previousNonMonthMode)
  }

  val selectedDate =
      Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()

  val targetMode =
      // The previous ViewMode was 5-days but the selected date is a weekend day.
      if (previousNonMonthMode == ViewMode.FIVE_DAYS && selectedDate.dayOfWeek.ordinal >= 5) {
        ViewMode.SEVEN_DAYS
      } else {
        previousNonMonthMode
      }

  val updatedRange =
      when (targetMode) {
        ViewMode.ONE_DAY -> LocalDateRange(selectedDate, selectedDate)
        ViewMode.FIVE_DAYS -> weekRangeContaining(selectedDate, days = 5)
        ViewMode.SEVEN_DAYS -> weekRangeContaining(selectedDate, days = 7)
        ViewMode.MONTH -> weekRangeContaining(selectedDate, days = 7) // Fallback.
      }

  return MonthPickerResult(
      updatedMode = targetMode,
      updatedRange = updatedRange,
      shouldShowMonthPicker = false,
      updatedPreviousNonMonthMode = previousNonMonthMode)
}

/**
 * Handles the dismiss action of the month picker without selecting a date.
 *
 * We return to the last non-month mode and keep the current date range.
 */
fun handleMonthPickerDismiss(
    previousNonMonthMode: ViewMode,
    currentRange: LocalDateRange,
): MonthPickerResult {
  return MonthPickerResult(
      updatedMode = previousNonMonthMode,
      updatedRange = currentRange,
      shouldShowMonthPicker = false,
      updatedPreviousNonMonthMode = previousNonMonthMode)
}

/**
 * Builds the click handler for the header days.
 *
 * In ONE_DAY mode, the header is clickable and updates the visible day. In other modes, the header
 * is not clickable, so we return null.
 */
fun buildOnHeaderDayClick(
    currentMode: ViewMode,
    onDaySelected: (LocalDate) -> Unit,
): ((LocalDate) -> Unit)? {
  return if (currentMode == ViewMode.ONE_DAY) {
    { clickedDate -> onDaySelected(clickedDate) }
  } else {
    null
  }
}

/**
 * Modifier that adds swipe gestures to change the visible date range.
 *
 * This keeps the composable body clean and focused on layout.
 */
private fun Modifier.calendarSwipeGestures(
    currentDateRange: LocalDateRange,
    currentMode: ViewMode,
    onRangeChanged: (LocalDateRange) -> Unit,
): Modifier {
  return pointerInput(currentDateRange, currentMode) {
    var totalDx = 0f

    // Handle horizontal swipes to navigate between days/weeks.
    detectDragGestures(
        onDrag = { _, dragAmount -> totalDx += dragAmount.x },
        onDragEnd = {
          val threshold = CalendarDefaults.DEFAULT_SWIPE_THRESHOLD
          val newRange =
              when {
                // Swipe right: go to previous day/week.
                totalDx > threshold ->
                    updateDateRangeForSwipe(
                        currentRange = currentDateRange,
                        currentMode = currentMode,
                        moveToPrevious = true,
                    )
                // Swipe left: go to next day/week.
                totalDx < -threshold ->
                    updateDateRangeForSwipe(
                        currentRange = currentDateRange,
                        currentMode = currentMode,
                        moveToPrevious = false,
                    )
                else -> currentDateRange
              }

          if (newRange != currentDateRange) {
            onRangeChanged(newRange)
          }
          totalDx = 0f
        },
        onDragCancel = { totalDx = 0f })
  }
}
