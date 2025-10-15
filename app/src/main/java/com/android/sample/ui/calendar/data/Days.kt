package com.android.sample.ui.calendar.data

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Returns the working days (Monday..Friday) for the week containing [today].
 *
 * @param today Any date within the target week; defaults to the current day.
 * @return A list from Monday to Friday inclusive.
 */
fun workWeekDays(today: LocalDate = LocalDate.now()): List<LocalDate> {
  val start = today.with(DayOfWeek.MONDAY)
  val end = today.with(DayOfWeek.FRIDAY)
  return generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
}

/**
 * Builds the inclusive list of dates between [start] and [endInclusive].
 *
 * @param start First day in the sequence.
 * @param endInclusive Last day in the sequence (included).
 * @return All dates from [start] to [endInclusive], inclusive.
 */
fun daysBetween(start: LocalDate, endInclusive: LocalDate): List<LocalDate> =
    generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(endInclusive) }.toList()

/**
 * Generates [count] consecutive days starting at [start].
 *
 * @param start First date of the sequence.
 * @param count Number of dates to return; must be non-negative.
 * @return A list of [count] dates from [start].
 * @throws IllegalArgumentException if [count] is negative.
 */
fun nextDays(start: LocalDate, count: Int): List<LocalDate> =
    generateSequence(start) { it.plusDays(1) }.take(count).toList()
