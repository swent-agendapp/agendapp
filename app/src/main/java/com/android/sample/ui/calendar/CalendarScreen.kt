package com.android.sample.ui.calendar

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.utils.TimeSpan
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    // initialize the week from monday to friday
    val today = LocalDate.now()

    val initialStartOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    val initialEndOfWeek = today.with(java.time.DayOfWeek.FRIDAY)

    val mockEvent1 = MockEvent(
        date = LocalDate.of(2025, 10, 7),
        title = "Cours de cirque 1",
        timeSpan = TimeSpan.of(
            start = LocalTime.of(9, 30),
            duration = Duration.ofHours(2)
        ),
        assigneeText = "Emilien",
        backgroundColor = Color.GREEN
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { paddingValues ->
        // Later : if we add button etc, it could be good to place this CalendarContainer in a Box
        CalendarContainer(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            // for now :
            event = mockEvent1
            // Later : give the ViewModel
            // Later : add here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
        )
    }
}