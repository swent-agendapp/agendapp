package com.android.sample.ui.replacement.route

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.replacement.Replacement
import com.android.sample.ui.organization.SelectedOrganizationVMProvider.viewModel
import com.android.sample.ui.replacement.ReplacementPendingListScreen
import com.android.sample.ui.replacement.ReplacementPendingViewModel

@Composable
fun ReplacementPendingRoute(
    onProcessReplacement: (Replacement) -> Unit,
    onBack: () -> Unit,
    viewModel: ReplacementPendingViewModel = viewModel(),
) {
  val state by viewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  if (state.isLoading) {
    CircularProgressIndicator()
  } else {
    ReplacementPendingListScreen(
        replacementsToProcess = state.toProcess,
        replacementsWaitingForAnswer = state.waitingForAnswer,
        onProcessReplacement = onProcessReplacement,
        onBack = onBack,
    )
  }
}
