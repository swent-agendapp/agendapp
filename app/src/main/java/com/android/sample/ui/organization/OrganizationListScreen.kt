package com.android.sample.ui.organization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.ui.common.MainPageTopBar

@Composable
fun OrganizationListScreen() {
  Scaffold(
      topBar = {
        MainPageTopBar(
            title = stringResource(R.string.organization_list_title),
        )
      }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
          {
            // Content for the Organization List Screen goes here
          }
        }
      }
}
