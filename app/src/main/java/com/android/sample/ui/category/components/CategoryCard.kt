package com.android.sample.ui.category.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.EditCategoryScreenTestTags
import com.android.sample.ui.theme.AlphaHigh
import com.android.sample.ui.theme.AlphaVeryHigh
import com.android.sample.ui.theme.CornerRadiusHuge
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.ElevationLow
import com.android.sample.ui.theme.ElevationNull
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    isDragging: Boolean,
    category: EventCategory,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
  // Determine which label to show
  val categoryLabel =
      if (category.isDefault) {
        stringResource(R.string.default_category_label)
      } else {
        category.label
      }

  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .alpha(if (isDragging) AlphaHigh else AlphaVeryHigh)
              .testTag(EditCategoryScreenTestTags.CATEGORY_CARD),
      elevation = CardDefaults.cardElevation(if (isDragging) ElevationNull else ElevationLow),
      shape = RoundedCornerShape(if (isDragging) CornerRadiusHuge else CornerRadiusLarge),
  ) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    horizontal = if (isDragging) PaddingMedium else PaddingSmall,
                    vertical = if (isDragging) PaddingMedium else PaddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
      // Left: color circle
      Spacer(modifier = Modifier.width(SpacingSmall))
      ColorCircle(color = category.color)
      Spacer(modifier = Modifier.width(SpacingMedium))

      // Middle: label text that can wrap
      Text(text = categoryLabel, modifier = Modifier.weight(WeightExtraHeavy))

      // Right: edit + delete actions
      Row {
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.testTag(EditCategoryScreenTestTags.CATEGORY_CARD_EDIT_BUTTON)) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.testTag(EditCategoryScreenTestTags.CATEGORY_CARD_DELETE_BUTTON)) {
              Icon(
                  imageVector = Icons.Default.DeleteForever,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error)
            }
      }
    }
  }
}
