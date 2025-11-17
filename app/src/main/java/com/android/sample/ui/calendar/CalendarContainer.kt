package com.android.sample.ui.calendar

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

  Box(modifier = modifier) {
    // When the mode is not MONTH, we show the week/day grid.
    if (currentMode != ViewMode.MONTH) {

      // Header shows the same range as the grid, except in ONE_DAY mode.
      // In ONE_DAY mode, the header shows the full week that contains the visible day.
      val headerDateRange =
          if (currentMode == ViewMode.ONE_DAY) {
            weekRangeContaining(currentDateRange.start, days = 7)
          } else {
            currentDateRange
          }

      // In ONE_DAY mode, this is the day currently displayed in the grid.
      // It will be highlighted as "selected" in the header.
      val selectedDate = if (currentMode == ViewMode.ONE_DAY) currentDateRange.start else null

      CalendarGridContent(
          modifier =
              Modifier.fillMaxSize().pointerInput(currentDateRange, currentMode) {
                var totalDx = 0f

                // Handle horizontal swipes to navigate between days/weeks.
                detectDragGestures(
                    onDrag = { _, dragAmount -> totalDx += dragAmount.x },
                    onDragEnd = {
                      val threshold = CalendarDefaults.DefaultSwipeThreshold
                      when {
                        // Swipe right: go to previous day/week.
                        totalDx > threshold -> {
                          currentDateRange =
                              updateDateRangeForSwipe(
                                  currentRange = currentDateRange,
                                  currentMode = currentMode,
                                  moveToPrevious = true,
                              )
                        }
                        // Swipe left: go to next day/week.
                        totalDx < -threshold -> {
                          currentDateRange =
                              updateDateRangeForSwipe(
                                  currentRange = currentDateRange,
                                  currentMode = currentMode,
                                  moveToPrevious = false,
                              )
                        }
                      }
                      totalDx = 0f
                    },
                    onDragCancel = { totalDx = 0f })
              },
          dateRange = currentDateRange,
          headerDateRange = headerDateRange,
          events = events,
          today = today,
          selectedDate = selectedDate,
          // Header is clickable only in ONE_DAY mode.
          onHeaderDayClick =
              if (currentMode == ViewMode.ONE_DAY) {
                { clickedDate ->
                  // When the user clicks a day in the header while in ONE_DAY mode,
                  // we update the visible range to show that single day.
                  currentDateRange = LocalDateRange(clickedDate, clickedDate)
                }
              } else {
                null
              },
          onEventClick = onEventClick)
    }

    // Floating button to change the view mode.
    ViewModeSelector(
        modifier =
            Modifier.align(Alignment.TopStart)
                .padding(start = PaddingExtraSmall, top = PaddingExtraSmall),
        currentMode = currentMode,
        onModeSelected = { newMode ->
          when (newMode) {
            ViewMode.MONTH -> {
              // Enter MONTH mode and show the month picker dialog.
              currentMode = ViewMode.MONTH
              showMonthPicker = true
            }
            else -> {
              // For non-month modes we remember this as the "last used" mode.
              val updatedRange =
                  updateDateRangeForModeChange(
                      previousMode = currentMode,
                      newMode = newMode,
                      currentRange = currentDateRange,
                      today = today,
                  )
              previousNonMonthMode = newMode
              currentMode = newMode
              currentDateRange = updatedRange
            }
          }
        })

    // MONTH mode: show the DatePickerModal instead of the grid.
    if (currentMode == ViewMode.MONTH && showMonthPicker) {
      DatePickerModal(
          onDateSelected = { selectedMillis ->
            // When a date is chosen, we reopen the corresponding day/week according to
            // the previously used non-month mode.
            val selectedDate =
                selectedMillis?.let {
                  Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }

            if (selectedDate != null) {
              val targetMode =
                  // The previous ViewMode was 5-days but the selected date is a weekend day.
                  if (previousNonMonthMode == ViewMode.FIVE_DAYS &&
                      selectedDate.dayOfWeek.ordinal >= 5) {
                    ViewMode.SEVEN_DAYS
                  } else {
                    previousNonMonthMode
                  }
              currentMode = targetMode
              currentDateRange =
                  when (targetMode) {
                    ViewMode.ONE_DAY -> LocalDateRange(selectedDate, selectedDate)
                    ViewMode.FIVE_DAYS -> weekRangeContaining(selectedDate, days = 5)
                    ViewMode.SEVEN_DAYS -> weekRangeContaining(selectedDate, days = 7)
                    ViewMode.MONTH -> weekRangeContaining(selectedDate, days = 7) // Fallback.
                  }
            } else {
              // No date selected: simply go back to the previous non-month mode.
              currentMode = previousNonMonthMode
            }

            showMonthPicker = false
          },
          onDismiss = {
            // If the dialog is dismissed, we return to the last non-month mode and keep the
            // previous date range.
            currentMode = previousNonMonthMode
            showMonthPicker = false
          })
    }
  }
}

/**
 * Loads the calendar events for a given date range using the provided [CalendarViewModel].
 *
 * Converts the [LocalDateRange] into corresponding [java.time.Instant] values covering the full
 * duration from start of the first day (midnight) to the end of the last day.
 */
private fun loadEventsForDateRange(
    calendarViewModel: CalendarViewModel,
    dateRange: LocalDateRange
) {
  calendarViewModel.loadEventsBetween(
      localDateTimeToInstant(dateRange.start, LocalTime.MIDNIGHT),
      localDateTimeToInstant(dateRange.endInclusive, LocalTime.MAX))
}

/**
 * Returns the default view mode:
 * - FIVE_DAYS if today is Monday–Friday
 * - SEVEN_DAYS if today is Saturday or Sunday
 */
private fun computeDefaultViewMode(today: LocalDate): ViewMode {
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
private fun computeInitialDateRange(today: LocalDate, mode: ViewMode): LocalDateRange {
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
private fun weekRangeContaining(date: LocalDate, days: Int): LocalDateRange {
  val monday = date.with(DayOfWeek.MONDAY)
  val end = monday.plusDays((days - 1).toLong())
  return LocalDateRange(monday, end)
}

/**
 * Updates the date range when the user swipes horizontally.
 * - In ONE_DAY mode: moves by 1 day forward/backward.
 * - In FIVE_DAYS or SEVEN_DAYS: moves by one whole week forward/backward (starting on Monday).
 */
private fun updateDateRangeForSwipe(
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
private fun updateDateRangeForModeChange(
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
