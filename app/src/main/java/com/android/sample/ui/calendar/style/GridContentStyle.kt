package com.android.sample.ui.calendar.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Color palette used by the grid background, labels, and the now indicator. */
@Immutable
data class GridContentColors(
    val todayHighlight: Color,
    val gridLineColor: Color,
    val nowIndicator: Color,
    val timeLabelTextColor: Color,
    val currentDayBackground: Color,
    val dayHeaderText: Color,
    val currentDayText: Color,
)

/**
 * Default color contract for grid elements. Values are remembered across recompositions.
 *
 * @param todayHighlight Background highlight color for today's column.
 * @param gridLineColor Color of hour and day separator lines.
 * @param nowIndicator Color of the current-time indicator line.
 * @param timeLabelTextColor Color of time tick labels.
 * @return A stable [GridContentColors] instance.
 */
@Composable
fun defaultGridContentColors(
    todayHighlight: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    gridLineColor: Color = Color.LightGray,
    nowIndicator: Color = MaterialTheme.colorScheme.error,
    timeLabelTextColor: Color = Color.Gray,
    currentDayBackground: Color = MaterialTheme.colorScheme.primary,
    currentDayText: Color = Color(0xFF000000),
    dayHeaderText: Color = Color.Gray,
): GridContentColors =
    remember(
        todayHighlight,
        gridLineColor,
        nowIndicator,
        timeLabelTextColor,
        currentDayBackground,
        currentDayText,
        dayHeaderText) {
          GridContentColors(
              todayHighlight = todayHighlight,
              gridLineColor = gridLineColor,
              nowIndicator = nowIndicator,
              timeLabelTextColor = timeLabelTextColor,
              currentDayBackground = currentDayBackground.copy(alpha = 0.2f),
              currentDayText = currentDayText,
              dayHeaderText = dayHeaderText,
          )
        }

/** Spacing and sizing values for the grid layout. */
@Immutable
data class GridContentDimensions(
    val leftOffsetDp: Dp,
    val topOffsetDp: Dp,
    val defaultColumnWidthDp: Dp,
    val rowHeightDp: Dp,
)

/**
 * Default dimensions for the grid layout. Values are remembered across recompositions.
 *
 * @param leftOffsetDp Width reserved for the time axis.
 * @param topOffsetDp Top padding above the first row (e.g., header height).
 * @param defaultColumnWidthDp Baseline width of a day column.
 * @param rowHeightDp Logical height of one hour row.
 * @return A stable [GridContentDimensions] instance.
 */
@Composable
fun defaultGridContentDimensions(
    leftOffsetDp: Dp = 58.dp,
    topOffsetDp: Dp = 58.dp,
    defaultColumnWidthDp: Dp = 64.dp,
    rowHeightDp: Dp = 60.dp,
): GridContentDimensions =
    remember(rowHeightDp) {
      GridContentDimensions(
          leftOffsetDp = leftOffsetDp,
          topOffsetDp = topOffsetDp,
          defaultColumnWidthDp = defaultColumnWidthDp,
          rowHeightDp = rowHeightDp)
    }

/** Bundles colors and dimensions for easier parameter passing across grid composables. */
@Immutable
data class GridContentStyle(
    val colors: GridContentColors,
    val dimensions: GridContentDimensions,

    // later : typography
)

/**
 * Creates a [GridContentStyle] from colors and dimensions, memoized with [remember].
 *
 * @param colors Color palette for the grid.
 * @param dimensions Dimension set for the grid.
 * @return A stable [GridContentStyle] instance.
 */
@Composable
fun defaultGridContentStyle(
    colors: GridContentColors = defaultGridContentColors(),
    dimensions: GridContentDimensions = defaultGridContentDimensions()
): GridContentStyle =
    remember(colors, dimensions) { GridContentStyle(colors = colors, dimensions = dimensions) }
