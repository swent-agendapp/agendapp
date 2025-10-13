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
    // TODO: nowIndicator, dayHeaderText, timeLabelTextColor, currentDayBackground, currentDayText
)

@Composable
fun defaultGridContentColors(
    todayHighlight: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    gridLineColor: Color = Color.LightGray,
): GridContentColors =
    remember(todayHighlight, gridLineColor) {
      GridContentColors(
          todayHighlight = todayHighlight,
          gridLineColor = gridLineColor,
      )
    }

@Immutable
data class GridContentDimensions(
    val rowHeightDp: Dp,
    // TODO: leftOffsetDp, topOffsetDp, defaultColumnWidthDp
)

@Composable
fun defaultGridContentDimensions(
    rowHeightDp: Dp = 60.dp,
): GridContentDimensions =
    remember(rowHeightDp) { GridContentDimensions(rowHeightDp = rowHeightDp) }

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
