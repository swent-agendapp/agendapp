package com.android.sample.ui.calendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.sample.R

@Composable
fun formatDecimalHoursToTime(decimalHours: Double): String {
  val hours = decimalHours.toInt()
  val minutes = ((decimalHours - hours) * 60).toInt()

  return if (minutes == 0) {
    stringResource(R.string.work_duration_hours, hours)
  } else {
    stringResource(R.string.work_duration_hours_minutes, hours, minutes)
  }
}
