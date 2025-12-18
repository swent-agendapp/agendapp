package com.android.sample.ui.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.theme.BorderWidthThick
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.theme.SizeMediumLarge

object ColorCircleTestTags {
  const val COLOR_CIRCLE = "color_circle"
}

@Composable
fun ColorCircle(
    modifier: Modifier = Modifier,
    color: Color = EventPalette.NoCategory,
    isSelected: Boolean = false,
    testTag: String = ColorCircleTestTags.COLOR_CIRCLE,
) {
  Box(
      modifier =
          modifier
              .size(SizeMediumLarge)
              .clip(CircleShape)
              .background(color)
              .border(
                  width = if (isSelected) BorderWidthThick else BorderWidthThin,
                  color = MaterialTheme.colorScheme.onSurface,
                  shape = CircleShape)
              .testTag(testTag),
  )
}
