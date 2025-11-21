package com.android.sample.ui.calendar

import com.android.sample.ui.calendar.components.ViewMode
import com.android.sample.ui.calendar.data.LocalDateRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Assisted by AI

/** Unit tests for ViewMode calculations. */
class ViewModeCalculationTest {

  // ---------------------------------------------------------------------------
  // Helpers for month logic tests
  // ---------------------------------------------------------------------------

  // This helper converts a LocalDate to epoch millis using the same time zone
  // as the production code (system default). This makes the date conversion
  // consistent between tests and production logic.
  private fun localDateToEpochMillis(date: LocalDate): Long {
    return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
  }

  // ---------------------------------------------------------------------------
  // handleViewModeSelection tests
  // ---------------------------------------------------------------------------

  @Test
  fun handleViewModeSelection_FromFiveDaysToMonth_ShouldKeepRangeAndOpenPicker() {
    // Given: we are in FIVE_DAYS with a normal work week.
    val currentMode = ViewMode.FIVE_DAYS
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 1, 6), // Monday
            LocalDate.of(2025, 1, 10), // Friday
        )
    val previousNonMonthMode = ViewMode.FIVE_DAYS
    val today = LocalDate.of(2025, 1, 8) // Today is inside the current week.

    // When: the user selects the MONTH mode.
    val result =
        handleViewModeSelection(
            newMode = ViewMode.MONTH,
            today = today,
            currentMode = currentMode,
            currentRange = currentRange,
            previousNonMonthMode = previousNonMonthMode,
        )

    // Then: we switch to MONTH, keep the same range, and open the month picker.
    assertEquals(ViewMode.MONTH, result.updatedMode)
    assertEquals(currentRange, result.updatedRange)
    assertTrue(result.shouldShowMonthPicker)
    // We still remember the same previous non-month mode.
    assertEquals(previousNonMonthMode, result.updatedPreviousNonMonthMode)
  }

  @Test
  fun handleViewModeSelection_FromMonthToFiveDays_ShouldUpdateRangeAndPreviousNonMonthMode() {
    // Given: we are in MONTH mode with a 7-day week range.
    val currentMode = ViewMode.MONTH
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 1, 6), // Monday
            LocalDate.of(2025, 1, 12), // Sunday
        )
    val previousNonMonthMode = ViewMode.SEVEN_DAYS
    val today = LocalDate.of(2025, 1, 8)

    // When: the user selects FIVE_DAYS from MONTH mode.
    val result =
        handleViewModeSelection(
            newMode = ViewMode.FIVE_DAYS,
            today = today,
            currentMode = currentMode,
            currentRange = currentRange,
            previousNonMonthMode = previousNonMonthMode,
        )

    // Then: we switch to FIVE_DAYS and compute a 5-day range from Monday to Friday.
    assertEquals(ViewMode.FIVE_DAYS, result.updatedMode)

    // Expected week: Monday to Friday of the same week as currentRange.start.
    val expectedStart = currentRange.start.with(DayOfWeek.MONDAY)
    val expectedEnd = expectedStart.plusDays(4)
    assertEquals(expectedStart, result.updatedRange.start)
    assertEquals(expectedEnd, result.updatedRange.endInclusive)

    // Previous non-month mode is updated to the new mode.
    assertEquals(ViewMode.FIVE_DAYS, result.updatedPreviousNonMonthMode)
    // The month picker must be hidden when we are not in MONTH mode.
    assertFalse(result.shouldShowMonthPicker)
  }

  // ---------------------------------------------------------------------------
  // handleMonthDateSelected tests
  // ---------------------------------------------------------------------------

  @Test
  fun handleMonthDateSelected_WithNullDate_ShouldReturnToPreviousModeAndKeepRange() {
    // Given: the month picker closes with no date selected (null).
    val selectedMillis: Long? = null
    val previousNonMonthMode = ViewMode.FIVE_DAYS
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 1, 6),
            LocalDate.of(2025, 1, 10),
        )

    // When: we handle the month picker result with no selection.
    val result =
        handleMonthDateSelected(
            selectedMillis = selectedMillis,
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: we return to the previous non-month mode and keep the range.
    assertEquals(previousNonMonthMode, result.updatedMode)
    assertEquals(currentRange, result.updatedRange)
    assertFalse(result.shouldShowMonthPicker)
    assertEquals(previousNonMonthMode, result.updatedPreviousNonMonthMode)
  }

  @Test
  fun handleMonthDateSelected_PreviousOneDay_ShouldOpenSelectedDay() {
    // Given: previous mode is ONE_DAY and the user selects a specific date.
    val previousNonMonthMode = ViewMode.ONE_DAY
    val selectedDate = LocalDate.of(2025, 3, 15)
    val selectedMillis = localDateToEpochMillis(selectedDate)
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 31),
        )

    // When: we handle the date selection from the month picker.
    val result =
        handleMonthDateSelected(
            selectedMillis = selectedMillis,
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: we go to ONE_DAY mode and show only the selected date.
    assertEquals(ViewMode.ONE_DAY, result.updatedMode)
    assertEquals(selectedDate, result.updatedRange.start)
    assertEquals(selectedDate, result.updatedRange.endInclusive)
    assertFalse(result.shouldShowMonthPicker)
  }

  @Test
  fun handleMonthDateSelected_PreviousSevenDays_ShouldOpenWeekOfSelectedDate() {
    // Given: previous mode is SEVEN_DAYS and the user selects a mid-week date.
    val previousNonMonthMode = ViewMode.SEVEN_DAYS
    val selectedDate = LocalDate.of(2025, 3, 12) // Wednesday
    val selectedMillis = localDateToEpochMillis(selectedDate)
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 28),
        )

    // When: we handle the date selection from the month picker.
    val result =
        handleMonthDateSelected(
            selectedMillis = selectedMillis,
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: we go to SEVEN_DAYS mode and show the full week of the selected date.
    assertEquals(ViewMode.SEVEN_DAYS, result.updatedMode)

    val expectedStart = selectedDate.with(DayOfWeek.MONDAY)
    val expectedEnd = expectedStart.plusDays(6)
    assertEquals(expectedStart, result.updatedRange.start)
    assertEquals(expectedEnd, result.updatedRange.endInclusive)

    assertFalse(result.shouldShowMonthPicker)
  }

  @Test
  fun handleMonthDateSelected_PreviousFiveDays_WithWeekday_ShouldStayInFiveDays() {
    // Given: previous mode is FIVE_DAYS and the user selects a weekday.
    val previousNonMonthMode = ViewMode.FIVE_DAYS
    val selectedDate = LocalDate.of(2025, 3, 11) // Tuesday
    val selectedMillis = localDateToEpochMillis(selectedDate)
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 28),
        )

    // When: we handle the date selection from the month picker.
    val result =
        handleMonthDateSelected(
            selectedMillis = selectedMillis,
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: we stay in FIVE_DAYS mode and show the Monday–Friday week.
    assertEquals(ViewMode.FIVE_DAYS, result.updatedMode)

    val expectedStart = selectedDate.with(DayOfWeek.MONDAY)
    val expectedEnd = expectedStart.plusDays(4)
    assertEquals(expectedStart, result.updatedRange.start)
    assertEquals(expectedEnd, result.updatedRange.endInclusive)

    assertFalse(result.shouldShowMonthPicker)
  }

  @Test
  fun handleMonthDateSelected_PreviousFiveDays_WithWeekend_ShouldSwitchToSevenDays() {
    // Given: previous mode is FIVE_DAYS and the user selects a weekend day.
    val previousNonMonthMode = ViewMode.FIVE_DAYS
    val selectedDate = LocalDate.of(2025, 3, 15) // Saturday
    val selectedMillis = localDateToEpochMillis(selectedDate)
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 28),
        )

    // When: we handle the date selection from the month picker.
    val result =
        handleMonthDateSelected(
            selectedMillis = selectedMillis,
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: we switch to SEVEN_DAYS to include the weekend properly.
    assertEquals(ViewMode.SEVEN_DAYS, result.updatedMode)

    val expectedStart = selectedDate.with(DayOfWeek.MONDAY)
    val expectedEnd = expectedStart.plusDays(6)
    assertEquals(expectedStart, result.updatedRange.start)
    assertEquals(expectedEnd, result.updatedRange.endInclusive)

    assertFalse(result.shouldShowMonthPicker)
  }

  @Test
  fun handleMonthDateSelected_PreviousFiveDays_WithWeekend_ShouldKeepPreviousNonMonthMode() {
    // Given: same weekend selection as before, but we check that we still remember FIVE_DAYS
    // as the preferred non-month mode.
    val previousNonMonthMode = ViewMode.FIVE_DAYS
    val selectedDate = LocalDate.of(2025, 3, 16) // Sunday
    val selectedMillis = localDateToEpochMillis(selectedDate)
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 28),
        )

    // When: we handle the weekend date from the month picker.
    val result =
        handleMonthDateSelected(
            selectedMillis = selectedMillis,
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: active mode is SEVEN_DAYS, but previousNonMonthMode is still FIVE_DAYS.
    assertEquals(ViewMode.SEVEN_DAYS, result.updatedMode)
    assertEquals(previousNonMonthMode, result.updatedPreviousNonMonthMode)
  }

  // ---------------------------------------------------------------------------
  // handleMonthPickerDismiss tests
  // ---------------------------------------------------------------------------

  @Test
  fun handleMonthPickerDismiss_ShouldReturnToPreviousModeAndKeepRange() {
    // Given: the month picker is dismissed without changing the selected date.
    val previousNonMonthMode = ViewMode.SEVEN_DAYS
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 4, 7),
            LocalDate.of(2025, 4, 13),
        )

    // When: we handle the dismiss action.
    val result =
        handleMonthPickerDismiss(
            previousNonMonthMode = previousNonMonthMode,
            currentRange = currentRange,
        )

    // Then: we return to the previous non-month mode and keep the date range.
    assertEquals(previousNonMonthMode, result.updatedMode)
    assertEquals(currentRange, result.updatedRange)
    assertFalse(result.shouldShowMonthPicker)
    assertEquals(previousNonMonthMode, result.updatedPreviousNonMonthMode)
  }

  // ---------------------------------------------------------------------------
  // updateDateRangeForSwipe tests (MONTH behavior fallback)
  // ---------------------------------------------------------------------------

  @Test
  fun updateDateRangeForSwipe_InMonthMode_ShouldMoveBySevenDaysWeek() {
    // Given: we are in MONTH mode and the current range is a Monday–Sunday week.
    val currentRange =
        LocalDateRange(
            LocalDate.of(2025, 5, 5), // Monday
            LocalDate.of(2025, 5, 11), // Sunday
        )
    val currentMode = ViewMode.MONTH

    // When: we swipe to the next period (moveToPrevious = false).
    val newRange =
        updateDateRangeForSwipe(
            currentRange = currentRange,
            currentMode = currentMode,
            moveToPrevious = false,
        )

    // Then: the new range is the next Monday–Sunday week.
    val expectedStart = currentRange.start.plusWeeks(1)
    val expectedEnd = expectedStart.plusDays(6)

    assertEquals(expectedStart, newRange.start)
    assertEquals(expectedEnd, newRange.endInclusive)
  }
}
