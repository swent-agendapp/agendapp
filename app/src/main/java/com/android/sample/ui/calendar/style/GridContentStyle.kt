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
    val nowIndicator: Color,
    val dayHeaderText: Color,
    val timeLabelTextColor: Color,
    val gridLineColor: Color,
    val currentDayBackground: Color,
    val currentDayText: Color,
)

@Composable
fun defaultGridContentColors(
    todayHighlight: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    nowIndicator: Color = MaterialTheme.colorScheme.error,
    dayHeaderText: Color = Color.Gray,
    timeLabelTextColor: Color = Color.Gray,
    gridLineColor: Color = Color.LightGray,
    currentDayBackground: Color = MaterialTheme.colorScheme.primary,
    currentDayText: Color = Color(0xFF000000),
): GridContentColors =
    remember(
        todayHighlight,
        nowIndicator,
        dayHeaderText,
        timeLabelTextColor,
        gridLineColor,
        currentDayBackground,
        currentDayText) {
          GridContentColors(
              todayHighlight = todayHighlight,
              nowIndicator = nowIndicator,
              dayHeaderText = dayHeaderText,
              timeLabelTextColor = timeLabelTextColor,
              gridLineColor = gridLineColor,
              currentDayBackground = currentDayBackground.copy(alpha = 0.2f),
              currentDayText = currentDayText,
          )
        }

// This is the main configuration object we pass around.
// It can be expanded later to include typography, dimensions, etc.
@Immutable
data class GridContentStyle( // agendap -> WeekTimeAxisStyle
    val colors: GridContentColors,
    val dimensions: GridContentDimensions
    // Later : val typography: WeekViewTypography,

)

@Immutable
data class GridContentDimensions(
    val leftOffsetDp: Dp,
    val topOffsetDp: Dp,
    val defaultColumnWidthDp: Dp,
    val rowHeightDp: Dp,
)

@Composable
fun defaultGridContentDimensions(
    leftOffsetDp: Dp = 48.dp,
    topOffsetDp: Dp = 42.dp,
    defaultColumnWidthDp: Dp = 64.dp,
    rowHeightDp: Dp = 60.dp,
): GridContentDimensions =
    remember(leftOffsetDp, topOffsetDp, defaultColumnWidthDp, rowHeightDp) {
        GridContentDimensions(
            leftOffsetDp = leftOffsetDp,
            topOffsetDp = topOffsetDp,
            defaultColumnWidthDp = defaultColumnWidthDp,
            rowHeightDp = rowHeightDp
        )
    }



@Composable
fun defaultGridContentStyle(
    colors: GridContentColors = defaultGridContentColors(),
    dimensions: GridContentDimensions = defaultGridContentDimensions()
): GridContentStyle =
    remember(colors) {
      GridContentStyle(
          colors = colors,
          dimensions = dimensions,
      )
    }
