package com.android.sample.ui.replacement.route

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.replacement.Replacement
import com.android.sample.ui.replacement.ReplacementPendingContract
import com.android.sample.ui.replacement.ReplacementPendingListScreen
import com.android.sample.ui.replacement.ReplacementPendingViewModel

object ReplacementPendingRouteTestTags {
  const val LOADING = "replacement_pending_route_loading"
}

@Composable
fun ReplacementPendingRoute(
    onProcessReplacement: (Replacement) -> Unit,
    onBack: () -> Unit,
    viewModel: ReplacementPendingContract = viewModel<ReplacementPendingViewModel>(),
) {
  val state by viewModel.uiState.collectAsState()

  LaunchedEffect(Unit) { viewModel.refresh() }

  if (state.isLoading) {
    CircularProgressIndicator(
        modifier = Modifier.testTag(ReplacementPendingRouteTestTags.LOADING),
    )
  } else {
    ReplacementPendingListScreen(
        replacementsToProcess = state.toProcess,
        replacementsWaitingForAnswer = state.waitingForAnswer,
        users = state.users,
        onProcessReplacement = onProcessReplacement,
        onBack = onBack,
    )
  }
}
