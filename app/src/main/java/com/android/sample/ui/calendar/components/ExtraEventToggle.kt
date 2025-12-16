package com.android.sample.ui.calendar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.sample.R
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingSmall

@Composable
fun ExtraEventToggle(
    isExtra: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    toggleTestTag: String? = null,
    descriptionTestTag: String? = null,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Text(
          text = stringResource(R.string.extra_event_label),
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
          modifier = Modifier.weight(1f))
      Switch(
          checked = isExtra,
          onCheckedChange = onToggle,
          modifier =
              toggleTestTag?.let { Modifier.testTag(it) } ?: Modifier)
    }
    Text(
        text =
            if (isExtra) stringResource(R.string.extra_event_description_on)
            else stringResource(R.string.extra_event_description_off),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.padding(top = SpacingSmall)
                .then(descriptionTestTag?.let { Modifier.testTag(it) } ?: Modifier)
                .padding(bottom = PaddingSmall))
  }
}
