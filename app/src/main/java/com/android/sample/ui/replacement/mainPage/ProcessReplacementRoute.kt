package com.android.sample.ui.replacement.mainPage

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.ui.replacement.ProcessReplacementScreen

@Composable
fun ProcessReplacementRoute(
    replacementId: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReplacementEmployeeViewModel = viewModel(),
) {
  ProcessReplacementScreen(
      replacementId = replacementId,
      onSendRequests = { selectedSubstitutes ->
        viewModel.sendRequestsForPendingReplacement(
            replacementId = replacementId,
            selectedSubstitutes = selectedSubstitutes,
            onFinished = onFinished,
        )
      },
      onBack = onBack,
  )
}
