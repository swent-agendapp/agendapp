package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.sp
import com.android.sample.ui.calendar.mockData.MockEvent

@Composable
fun EventBlock(
    modifier: Modifier = Modifier,
    event: MockEvent
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color(event.backgroundColor))
    ) {
        Column {
            Text(text = event.title, fontSize = 12.sp)
            Text(text = event.assigneeText, fontSize = 10.sp)
        }
    }
}