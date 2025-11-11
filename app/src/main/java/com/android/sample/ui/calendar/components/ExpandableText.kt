package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified

/**
 *  Aggregates localized labels used by the expand/collapse toggle
 * (like "Show more" / "Show less).
 */
data class ToggleLabels(val expand: String, val collapse: String)

/**
 * Reusable expandable text with an optional last-line end fade and a trailing toggle button.
 *
 * Behavior:
 * - Collapsed mode renders up to `collapsedMaxLines` and reports overflow via [onOverflowChange].
 * - When `showToggle` is true in collapsed mode, a right-edge horizontal fade is drawn on the last
 *   rendered line only, aligned to the actual text end.
 * - Expanded mode removes line limits and keeps the toggle visible (e.g. as "Show less").
 *
 * Implementation notes:
 * - The fade width scales with the text style's font size for consistent appearance.
 * - If the style has no explicit `lineHeight`, we approximate as `fontSize * 1.2f`.
 * - The fade uses `BlendMode.DstIn` on an offscreen layer so only text pixels are affected.
 *
 * @param text Text content to display.
 * @param style Text style used for measurement and rendering.
 * @param collapsedMaxLines Maximum number of lines when collapsed.
 * @param isExpanded Whether the text is expanded.
 * @param onToggleExpand Invoked when the user toggles expansion.
 * @param onOverflowChange Reports whether the collapsed text visually overflows.
 * @param showToggle Whether the toggle button should be shown.
 * @param toggleLabels Localized labels for the toggle button.
 * @param toggleTypography Typography used for the toggle button.
 * @param modifier Optional modifier for the root column.
 * @param onTextHeightChange Called with the measured height in pixels.
 * @param toggleTestTag Optional test tag applied to the toggle for UI tests.
 */
@Composable
fun ExpandableText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    collapsedMaxLines: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOverflowChange: (Boolean) -> Unit,
    showToggle: Boolean,
    toggleLabels: ToggleLabels,
    toggleTypography: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    onTextHeightChange: (Int) -> Unit = {},
    toggleTestTag: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // If no explicit lineHeight, use a conservative multiplier to avoid cramped lines
        val density = LocalDensity.current
        val fontSizePx = with(density) { style.fontSize.toPx() }
        val computedLineHeight =
            if (style.lineHeight.isUnspecified) style.fontSize * 1.2f else style.lineHeight
        val lineHeightPx = with(density) { computedLineHeight.toPx() }
        // Scale fade width with font size for consistent perceived length
        val fadeWidthPx = fontSizePx * 12f

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = text,
                style = style,
                maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
                overflow = TextOverflow.Clip,
                softWrap = isExpanded || collapsedMaxLines > 1,
                onTextLayout = { result ->
                    // When expanded, keep the toggle visible as "Show less"
                    if (!isExpanded) onOverflowChange(result.hasVisualOverflow) else onOverflowChange(true)
                },
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier.onSizeChanged { onTextHeightChange(it.height) }
                        .then(
                            if (!isExpanded && showToggle) {
                                // Draw content first, then punch in a right-edge fade on ONLY the last visible line
                                Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                    .drawWithContent {
                                        drawContent()
                                        // Target the vertical band of the last line only
                                        val endX = size.width
                                        val startX = (endX - fadeWidthPx).coerceAtLeast(0f)
                                        val topY = (size.height - lineHeightPx).coerceAtLeast(0f)
                                        val width = (endX - startX).coerceAtLeast(0f)
                                        if (width > 0f && lineHeightPx > 0f) {
                                            drawRect(
                                                brush =
                                                    Brush.horizontalGradient(
                                                        colors = listOf(Color.Black, Color.Transparent),
                                                        startX = startX,
                                                        endX = endX),
                                                topLeft = Offset(startX, topY),
                                                size = Size(width, lineHeightPx),
                                                blendMode = BlendMode.DstIn)
                                        }
                                    }
                            } else {
                                Modifier
                            }))
        }

        // Toggle row aligned to end, remains visible in expanded mode (e.g. as "Show less")
        if (showToggle) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp),
                    modifier = if (toggleTestTag != null) Modifier.testTag(toggleTestTag) else Modifier) {
                    Text(
                        text = if (isExpanded) toggleLabels.collapse else toggleLabels.expand,
                        style = toggleTypography,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            // Keep vertical rhythm when no toggle is shown
            Spacer(Modifier.height(8.dp))
        }
    }
}