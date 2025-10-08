package com.android.sample.ui.calendar.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

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
    // Later : val typography: WeekViewTypography,
    // Later : val dimensions: WeekViewDimensions
)

@Composable
fun defaultGridContentStyle(
    colors: GridContentColors = defaultGridContentColors()
): GridContentStyle =
    remember(colors) {
      GridContentStyle(
          colors = colors,
      )
    }
