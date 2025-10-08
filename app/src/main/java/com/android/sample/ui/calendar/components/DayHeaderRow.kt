package com.android.sample.ui.calendar.components

import android.text.format.DateUtils.isToday
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale



@Composable
fun DayHeaderRow(
    days: List<LocalDate>,
    leftOffsetDp: Dp,
    topOffsetDp: Dp,
    columnWidth: Dp
    ){
    Row(
        modifier= Modifier.fillMaxWidth().height(topOffsetDp)
    ){
        Box(modifier = Modifier.width(leftOffsetDp).fillMaxHeight())
        days.forEach { date ->
            val isToday = date == LocalDate.now()
            val bg = if (isToday) {
                Color(0x112196F3)
            } else {
                Color.Transparent
            }

            val color = if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            val weight = if (isToday) FontWeight.Bold else FontWeight.Medium

            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val shortDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .format(date)
                .replace(Regex("[^0-9]*[0-9]+$"), "")

            Column(
                modifier = Modifier
                    .width(columnWidth)
                    .fillMaxHeight()
                    .background(bg)
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = weight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = shortDate,
                    maxLines = 1,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = weight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}