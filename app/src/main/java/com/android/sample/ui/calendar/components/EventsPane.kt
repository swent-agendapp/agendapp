package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.mockData.MockEvent

@Composable
fun EventsPane(event: MockEvent) {
    Box(
        modifier = Modifier
            .offset(x = 80.dp, y = 80.dp) // Later : x = dayIndex * columnWidth
            .size(120.dp, 60.dp) // Later : (columnWidth, gridHeightDp)
    ) {
        // for now :
        EventBlock(
            event = event
        )
        // Later : EventsWithOverlapHandling
    }
}