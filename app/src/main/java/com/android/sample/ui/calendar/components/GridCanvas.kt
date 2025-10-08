package com.android.sample.ui.calendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

private fun defaultWeekDays(): List<LocalDate>{
    val today = LocalDate.now()
    val monday = today.with(java.time.DayOfWeek.MONDAY)
    return (0 until 5).map{monday.plusDays(it.toLong())}
}

data class WeekGridStyle(
    val gridLineColor: Color = Color(0xFFE0E0E0),
    val todayHighlight: Color = Color(0x112196F3),
    val nowIndicator: Color = Color(0xFF2196F3)
)


@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    columnCount: Int = 5,
    rowHeightDp: Dp = 60.dp,
    totalHours: Float = 12f,
    days: List<LocalDate> =  defaultWeekDays(),
    now: LocalTime = LocalTime.now(),
    gridStartTime: LocalTime = LocalTime.of(8,0),
    effectiveEndTime: LocalTime = LocalTime.of(22,0),
    style: WeekGridStyle = WeekGridStyle(),
) {
    Canvas(modifier = modifier){
        val columnWidthPx = if (columnCount > 0) size.width / columnCount else size.width
        val rowHeightPx = rowHeightDp.toPx()

        for (i in 0..columnCount) {
            val x = i * columnWidthPx
            drawLine(
                color = style.gridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2f
            )
        }
        val hourLineCount = ceil(totalHours).toInt()
        for (i in 0..hourLineCount) {
            val y = i * rowHeightPx
            drawLine(
                color = style.gridLineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
        }
        val today = LocalDate.now()
        val todayIndex = days.indexOf(today)
        if (todayIndex in 0 until columnCount) {
            val left = todayIndex * columnWidthPx
            drawRect(
                color = style.todayHighlight,
                topLeft = Offset(left, 0f),
                size = Size(columnWidthPx, size.height)
            )
        }
        if (now.isAfter(gridStartTime) && now.isBefore(effectiveEndTime) && todayIndex in 0 until columnCount) {
            val minutesFromStart = ChronoUnit.MINUTES.between(gridStartTime, now)
            val nowY = (minutesFromStart / 60f) * rowHeightPx
            if (nowY in 0f..size.height) {
                val left = todayIndex * columnWidthPx
                val right = left + columnWidthPx
                drawLine(
                    color = style.nowIndicator,
                    start = Offset(left, nowY),
                    end = Offset(right, nowY),
                    strokeWidth = 4f
                )
                drawCircle(
                    color = style.nowIndicator,
                    radius = 8f,
                    center = Offset(left, nowY)
                )
            }
        }

    }
}