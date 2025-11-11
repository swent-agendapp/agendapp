package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags

@Composable
fun ColoredSideBar(width: Dp, color: Color, shape: RoundedCornerShape) {
    Box(
        modifier =
            Modifier.width(width)
                .fillMaxHeight()
                .background(
                    color,
                    shape =
                        RoundedCornerShape(
                            topStart = shape.topStart,
                            bottomStart = shape.bottomStart,
                            topEnd = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp)))
                .testTag(EventSummaryCardTags.SideBar))
}