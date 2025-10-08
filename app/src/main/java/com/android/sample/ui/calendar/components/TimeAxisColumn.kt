package com.android.sample.ui.calendar.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun TimeAxisColumn(
    timeLabels: List<LocalTime>,
    rowHeightDp: Dp,
    gridHeightDp: Dp,
    leftOffsetDp: Dp,
    style: GridContentStyle = defaultGridContentStyle(),
    scrollState: ScrollState
) {
    Box(
        modifier =
            Modifier
                .width(leftOffsetDp)
                .height(gridHeightDp),
        // Total height of the scrollable grid
    ) {
        // Regular time labels (hours)
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            timeLabels.forEach { timeLabel ->
                Box(modifier = Modifier.size(leftOffsetDp, rowHeightDp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = timeLabel.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = TextStyle(fontSize = 12.sp, color = style.colors.timeLabelTextColor),
                    )
                }
            }
        }
    }
}