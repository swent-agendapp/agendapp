package com.android.sample.ui.calendar.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

data class WeekTimeAxisStyle(
    val gridLineColor: Color = Color(0xFFE0E0E0),
    val todayHighlight: Color = Color(0x112196F3),
    val nowIndicator: Color = Color(0xFF2196F3),
    val timeLabelTextColor: Color = Color.Black,
    val nowIndicatorColor: Color = Color(0xFF2196F3)
)
@Composable
internal fun TimeAxisColumn(
    timeLabels: List<LocalTime>,
    now: LocalTime,
    gridStartTime: LocalTime,
    gridEndTime: LocalTime,
    rowHeightDp: Dp,
    gridHeightDp: Dp,
    leftOffsetDp: Dp,
    style: WeekTimeAxisStyle = WeekTimeAxisStyle()
    // Later : scrollState: ScrollState,
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
            // Later : modifier = Modifier.verticalScroll(scrollState)
        ) {
            timeLabels.forEach { timeLabel ->
                Box(modifier = Modifier.size(leftOffsetDp, rowHeightDp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = timeLabel.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = TextStyle(fontSize = 12.sp, color = style.timeLabelTextColor),
                    )
                }
            }
        }

        // Current time indicator label (HH:mm)
        if (now.isAfter(gridStartTime) && now.isBefore(gridEndTime)) {
            val nowPositionMinutes = ChronoUnit.MINUTES.between(gridStartTime, now)
            val nowPositionDp = (nowPositionMinutes / 60f * rowHeightDp.value).dp
            val density = LocalDensity.current.density

            Box(
                modifier =
                    Modifier
                        .offset(y = nowPositionDp /* later : - (scrollState.value / density).dp */ - 12.dp)
                        .width(leftOffsetDp)
                        .height(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style =
                        TextStyle(
                            fontSize = 12.sp,
                            color = style.nowIndicatorColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                )
            }
        }
    }
}