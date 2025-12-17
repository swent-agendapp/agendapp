package com.android.sample.ui.calendar.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.GeneralPaletteDark
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.SpacingSmall

@Composable
fun FieldLabelWithIcon(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    label: String = "",
) {
  val surfaceVariant =
      if (isSystemInDarkTheme()) GeneralPaletteDark.SurfaceVariant
      else GeneralPalette.SurfaceVariant

  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Surface(shape = MaterialTheme.shapes.small, color = surfaceVariant) {
      Box(modifier = Modifier.padding(PaddingExtraSmall), contentAlignment = Alignment.Center) {
        icon()
      }
    }
    Spacer(modifier = Modifier.padding(horizontal = SpacingSmall))
    Text(text = label, style = MaterialTheme.typography.labelLarge)
  }
}
