package com.android.sample.ui.calendar.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GridContentColors(
    val todayHighlight: Color,
    val gridLineColor: Color,
    val nowIndicator: Color,
    val timeLabelTextColor: Color,
    // TODO:  dayHeaderText, currentDayBackground, currentDayText
)

@Composable
fun defaultGridContentColors(
    todayHighlight: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    gridLineColor: Color = Color.LightGray,
    nowIndicator: Color = MaterialTheme.colorScheme.error,
    timeLabelTextColor: Color = Color.Gray
): GridContentColors =
    remember(todayHighlight, gridLineColor, nowIndicator, timeLabelTextColor) {
      GridContentColors(
          todayHighlight = todayHighlight,
          gridLineColor = gridLineColor,
          nowIndicator = nowIndicator,
          timeLabelTextColor = timeLabelTextColor,
      )
    }

@Immutable
data class GridContentDimensions(
    val rowHeightDp: Dp,
    val leftOffsetDp: Dp,
    // TODO: topOffsetDp, defaultColumnWidthDp
)

@Composable
fun defaultGridContentDimensions(
    rowHeightDp: Dp = 60.dp,
    leftOffsetDp: Dp = 48.dp,
): GridContentDimensions =
    remember(rowHeightDp) {
      GridContentDimensions(
          rowHeightDp = rowHeightDp,
          leftOffsetDp = leftOffsetDp,
      )
    }

@Immutable
data class GridContentStyle(
    val colors: GridContentColors,
    val dimensions: GridContentDimensions,

    // TODO: typography
)

@Composable
fun defaultGridContentStyle(
    colors: GridContentColors = defaultGridContentColors(),
    dimensions: GridContentDimensions = defaultGridContentDimensions()
): GridContentStyle =
    remember(colors, dimensions) { GridContentStyle(colors = colors, dimensions = dimensions) }
