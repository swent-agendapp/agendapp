package com.android.sample.ui.replacement.route

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.authentication.User
import com.android.sample.model.replacement.Replacement
import com.android.sample.ui.replacement.ProcessReplacementScreen
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeActions
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeViewModel

object ProcessReplacementRouteTestTags {
  const val LOADING_INDICATOR = "ProcessReplacementRoute_LOADING"
}

@Composable
fun ProcessReplacementRoute(
    replacementId: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReplacementEmployeeActions = viewModel<ReplacementEmployeeViewModel>(),
) {
  var replacement by remember { mutableStateOf<Replacement?>(null) }
  var users by remember { mutableStateOf<List<User>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }

  LaunchedEffect(replacementId) {
    viewModel.loadReplacementForProcessing(
        replacementId = replacementId,
        onResult = { inReplacement, inUsers ->
          replacement = inReplacement
          users = inUsers
          isLoading = false
        })
  }

  when {
    isLoading -> {
      CircularProgressIndicator(
          modifier = Modifier.testTag(ProcessReplacementRouteTestTags.LOADING_INDICATOR))
    }
    replacement == null -> {
      onBack()
    }
    else -> {
      ProcessReplacementScreen(
          replacement = replacement!!,
          candidates = users.filter { it.id != replacement!!.absentUserId },
          users = users,
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
  }
}
