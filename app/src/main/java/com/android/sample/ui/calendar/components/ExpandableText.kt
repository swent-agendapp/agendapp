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

/** Tiny struct to group toggle labels (like "Show more" and "Show less"). */
data class ToggleLabels(val expand: String, val collapse: String)

/**
 * Reusable expandable text with optional edge fade and toggle button.
 * - Reports overflow via [onOverflowChange].
 * - Keeps vertical rhythm when toggle is absent by adding a small spacer.
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
        // Measurements derived from the provided text style
        val density = LocalDensity.current
        val fontSizePx = with(density) { style.fontSize.toPx() }
        val computedLineHeight =
            if (style.lineHeight.isUnspecified) style.fontSize * 1.2f else style.lineHeight
        val lineHeightPx = with(density) { computedLineHeight.toPx() }
        // Fade width scales with font size
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
                                Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                    .drawWithContent {
                                        drawContent()
                                        // Apply fade aligned to the actual end of the text content
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
            Spacer(Modifier.height(8.dp))
        }
    }
}
