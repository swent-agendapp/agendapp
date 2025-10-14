package com.android.sample.ui.calendar.data

import java.time.DayOfWeek
import java.time.LocalDate

fun workWeekDays(today: LocalDate = LocalDate.now()): List<LocalDate> {
  val start = today.with(DayOfWeek.MONDAY)
  val end = today.with(DayOfWeek.FRIDAY)
  return generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
}

fun daysBetween(start: LocalDate, endInclusive: LocalDate): List<LocalDate> =
    generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(endInclusive) }.toList()

fun nextDays(start: LocalDate, count: Int): List<LocalDate> =
    generateSequence(start) { it.plusDays(1) }.take(count).toList()
