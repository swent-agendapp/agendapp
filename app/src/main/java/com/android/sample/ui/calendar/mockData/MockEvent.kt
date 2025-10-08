package com.android.sample.ui.calendar.mockData;

import com.android.sample.ui.calendar.utils.TimeSpan
import java.time.Duration
import java.time.LocalDate;
import java.time.LocalTime

data class MockEvent (
    val date: LocalDate,
    val title: String,
    val timeSpan: TimeSpan,
    val assigneeText: String,
    val backgroundColor: Int
) {
    companion object {
        fun getMockEvents(): List<MockEvent> {
            return listOf(
                MockEvent(
                    date = LocalDate.of(2025, 10, 6),
                    title = "Nice event",
                    timeSpan = TimeSpan.of(
                        start = LocalTime.of(9, 30),
                        duration = Duration.ofHours(2)
                    ),
                    assigneeText = "Emilien",
                    backgroundColor = 0xFFE57373.toInt()
                ),
                MockEvent(
                    date = LocalDate.of(2025, 10, 7),
                    title = "Great event",
                    timeSpan = TimeSpan.of(
                        start = LocalTime.of(14, 0),
                        duration = Duration.ofHours(4)
                    ),
                    assigneeText = "Méline",
                    backgroundColor = 0xFF81C784.toInt()
                ),
                MockEvent(
                    date = LocalDate.of(2025, 10, 8),
                    title = "Top Event",
                    timeSpan = TimeSpan.of(
                        start = LocalTime.of(11, 0),
                        duration = Duration.ofHours(2)
                    ),
                    assigneeText = "Nathan",
                    backgroundColor = 0xFF64B5F6.toInt()
                )
            )
        }
    }
}
