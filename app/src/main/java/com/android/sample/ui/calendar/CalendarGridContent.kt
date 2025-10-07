package com.android.sample.ui.calendar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import com.android.sample.ui.calendar.components.EventsPane
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.mockData.MockEvent
@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    // for now :
    event: MockEvent
    // Later : receive dateRange and events
    // Later : receive onEventClick and onEventLongPress
) {
    // todo : add metrics tool
    // Later : handle scroll using val scrollState = rememberScrollState()
    // Later : handle "now" pointer using var now by remember { mutableStateOf(LocalTime.now()) }

    // Later : use BoxWithConstraints to adapt column width
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // todo : DayHeaderRow

            Row {
                // todo : TimeAxisColumn

                // Scrollable Grid Area (Canvas + Events)
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    // Render the grid background
                    GridCanvas()
                    // Render all the events blocks
                    EventsPane(event = event)
                }
            }
        }
    }
}