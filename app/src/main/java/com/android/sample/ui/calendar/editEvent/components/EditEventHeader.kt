package com.android.sample.ui.calendar.editEvent.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.GeneralPaletteDark
import com.android.sample.ui.theme.PaddingSmallMedium
import com.android.sample.ui.theme.SpacingSmall

@Composable
fun EditEventHeaderRow(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.edit_event_header_step),
) {
  val surfaceVariant =
      if (isSystemInDarkTheme()) GeneralPaletteDark.SurfaceVariant
      else GeneralPalette.SurfaceVariant
  val onSurfaceVariant =
      if (isSystemInDarkTheme()) GeneralPaletteDark.OnSurfaceVariant
      else GeneralPalette.OnSurfaceVariant

  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Start) {
        Surface(shape = MaterialTheme.shapes.large, color = surfaceVariant) {
          Box(
              modifier = Modifier.padding(PaddingSmallMedium),
              contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
              }
        }

        Spacer(modifier = Modifier.width(SpacingSmall))

        Text(text = title, style = MaterialTheme.typography.labelLarge, color = onSurfaceVariant)
      }
}
