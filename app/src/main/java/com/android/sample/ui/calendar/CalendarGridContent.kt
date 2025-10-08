package com.android.sample.ui.calendar

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.sample.ui.calendar.components.EventsPane
import com.android.sample.ui.calendar.components.GridCanvas
import com.android.sample.ui.calendar.mockData.MockEvent
import com.android.sample.ui.calendar.utils.LocalDateRange
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CalendarGridContent(
    modifier: Modifier = Modifier,
    dateRange: LocalDateRange,
    events: List<MockEvent>
    // Later : receive onEventClick and onEventLongPress
) {
    val metrics = rememberWeekViewMetrics(dateRange, events)

    // Later : handle scroll using val scrollState = rememberScrollState()
    // Later : handle "now" pointer using var now by remember { mutableStateOf(LocalTime.now()) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth - metrics.leftOffsetDp
        val dynamicColumnWidthDp = if (metrics.columnCount > 0) (availableWidth / metrics.columnCount) else availableWidth

        Column(modifier = Modifier.fillMaxSize()) {
            // todo : DayHeaderRow
//            DayHeaderRow(
//                days = metrics.days,
//                leftOffsetDp = metrics.leftOffsetDp,
//                topOffsetDp = metrics.topOffsetDp,
//                columnWidth = dynamicColumnWidthDp
//            )

            Row {
                // todo : TimeAxisColumn

                // for now :            Grid Area (Canvas + Events)
                // Later :   Scrollable Grid Area (Canvas + Events)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(metrics.gridHeightDp),
                    ) {
                    // Render the grid background
                    GridCanvas(
                        modifier = Modifier.matchParentSize()
                    )

                    // Render all the events blocks
                    EventsPane(
                        days = metrics.days,
                        events = events,
                        columnWidthDp = dynamicColumnWidthDp,
                        gridHeightDp = metrics.gridHeightDp,
                        gridStartTime = metrics.gridStartTime,
                        effectiveEndTime = metrics.effectiveEndTime
                    )
                }
            }
        }
    }
}