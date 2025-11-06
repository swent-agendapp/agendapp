package com.android.sample.ui.calendar.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.sample.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    canNavigateBack: Boolean = false,
    onBack: () -> Unit = {},
    canLogOut: Boolean = false,
    onLogOut: () -> Unit = {}
) {
  TopAppBar(
      title = {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier.testTag(""), // later : NavigationTestTags.TOP_BAR_TITLE
            fontWeight = FontWeight.Bold)
      },
      navigationIcon = {
        if (canNavigateBack) {
          IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("")) { // later : NavigationTestTags.GO_BACK_BUTTON
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.top_bar_back_content_description))
              }
        }
      },
      actions = {
        if (canLogOut) {
          IconButton(
              onClick = onLogOut,
              modifier = Modifier.testTag("")) { // later : NavigationTestTags.LOGOUT_BUTTON
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Logout,
                    contentDescription =
                        stringResource(R.string.top_bar_logout_content_description))
              }
        }
      },
      modifier = modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))
}
