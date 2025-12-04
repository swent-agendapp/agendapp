package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.theme.AlphaMedium
import com.android.sample.ui.theme.IconSizeMedium
import com.android.sample.ui.theme.SpacingSmall

@Composable
fun CategorySection(category: EventCategory = EventCategory.defaultCategory()) {
  val categoryLabel =
      if (category.isDefault) {
        stringResource(R.string.default_category_label)
      } else category.label

  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Filled.SubdirectoryArrowRight,
        contentDescription = null,
        modifier = Modifier.size(IconSizeMedium),
        tint = category.color) // .copy(alpha = AlphaLow))
    Spacer(Modifier.width(SpacingSmall))
    Text(
        text = categoryLabel,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaMedium),
        modifier = Modifier.testTag(EventSummaryCardTags.CATEGORY))
  }
}
