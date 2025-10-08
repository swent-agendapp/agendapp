package com.android.sample.ui.calendar

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
import com.android.sample.ui.calendar.utils.LocalDateRange
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    // initialize the week from monday to friday
    val today = LocalDate.now()

    val initialStartOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    val initialEndOfWeek = today.with(java.time.DayOfWeek.FRIDAY)
    val currentDateRange by remember { mutableStateOf(LocalDateRange(initialStartOfWeek, initialEndOfWeek)) }

    // for now : create mock events
    val mockEvents = MockEvent.getMockEvents()

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
            dateRange = currentDateRange,
            // for now :
            events = mockEvents
            // Later : give the ViewModel
            // Later : add here onEventClick, onEventLongPress, onSwipeLeft, onSwipeRight
        )
    }
}