package com.android.sample.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.map.BottomSheetState
import com.android.sample.ui.map.MapScreenTestTags.CREATE_AREA_FLOATING_BUTTON
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.Salmon
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.WeightLight
import com.android.sample.ui.theme.WeightMedium
import com.android.sample.ui.theme.heightLarge
import com.android.sample.ui.theme.widthLarge


/**
 * Displays a single replacement option card.
 *
 * The card shows an icon and a title, and executes a callback when clicked.
 *
 */
@Composable
@Preview
fun FloatingButton(
  modifier: Modifier = Modifier,
  text: String = "Test",
  onClick: () -> Unit = {},
  icon: ImageVector = Icons.Default.Add
) {
  ExtendedFloatingActionButton(
    modifier = modifier,
    text = { Text(text) },
    icon = {
      Icon(
       icon,
        contentDescription = stringResource(R.string.create_area_button))
    },
    onClick = onClick,
    containerColor = Salmon,
    contentColor = Color.White
    )
}
