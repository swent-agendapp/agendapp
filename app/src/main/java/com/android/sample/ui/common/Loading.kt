package com.android.sample.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.sample.ui.theme.BorderWidthExtraThick
import com.android.sample.ui.theme.PaddingMedium

@Composable
fun Loading(modifier: Modifier = Modifier, label: String? = null) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(strokeWidth = BorderWidthExtraThick)
    Text(
        text = label ?: "",
        modifier = Modifier.align(Alignment.BottomCenter).padding(top = PaddingMedium))
  }
}
