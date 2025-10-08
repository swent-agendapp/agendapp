package com.android.sample.ui.calendar.mockData

import com.android.sample.ui.calendar.utils.TimeSpan
import java.time.LocalDate

data class MockEvent(
    val date: LocalDate,
    val title: String,
    val timeSpan: TimeSpan,
    val assigneeText: String,
    val backgroundColor: Int
)
