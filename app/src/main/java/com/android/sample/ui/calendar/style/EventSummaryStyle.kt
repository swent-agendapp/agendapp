package com.android.sample.ui.calendar.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.components.ToggleLabels
import com.android.sample.ui.theme.*

/** Style knobs grouped in one place to centralize all layout parameters. */
data class EventSummaryCardStyle(
    val cornerRadiusDp: Dp = CornerRadiusHuge,
    val leftBarWidthDp: Dp = BarWidthMedium,
    val paddingH: Dp = PaddingLarge,
    val paddingV: Dp = PaddingMedium,
    val titleSpacer: Dp = PaddingMedium,
    val sectionGapLarge: Dp = SpacingHuge,
    val sectionGapSmall: Dp = SpacingMedium,
    val descNoToggleSpacer: Dp = SpacingExtraLarge,
    val descHasToggleSpacer: Dp = SpacingSmall,
    val participantsRowHeight: Dp = RowHeightMedium,
    val participantsVisibleRows: Int = 5,
    val participantsBorderColor: @Composable () -> Color = {
      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaLow)
    },
    val participantsHeaderColor: @Composable () -> Color = {
      MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh)
    },
    val participantsRowColorEvenLine: @Composable () -> Color = {
      MaterialTheme.colorScheme.surface
    },
    val participantsRowColorOddLine: @Composable () -> Color = {
      MaterialTheme.colorScheme.surfaceVariant
    },
    val participantsNameTextColor: @Composable () -> Color = {
      MaterialTheme.colorScheme.onSurface
    },
)

/** Labels and collapsed line limits. */
data class EventSummaryTextConfig(
    val titleCollapsedMaxLines: Int = 2,
    val descriptionCollapsedMaxLines: Int = 3,
    val toggleLabels: ToggleLabels = ToggleLabels("Show more", "Show less")
)

/** Consolidated defaults for callers. */
object EventSummaryCardDefaults {
  val style = EventSummaryCardStyle()
  val texts = EventSummaryTextConfig()
}
