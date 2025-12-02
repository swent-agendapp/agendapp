package com.android.sample.ui.replacement.route

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.replacement.Replacement
import com.android.sample.ui.calendar.replacementEmployee.ReplacementEmployeeViewModel
import com.android.sample.ui.replacement.ProcessReplacementScreen
import com.android.sample.ui.replacement.ReplacementPendingListScreen
import com.android.sample.ui.replacement.ReplacementPendingViewModel

@Composable
fun ProcessReplacementRoute(
    replacementId: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReplacementEmployeeViewModel = viewModel(),
) {
    var replacement by remember { mutableStateOf<Replacement?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(replacementId) {
        viewModel.loadReplacementForProcessing(
            replacementId = replacementId,
            onResult = {
                replacement = it
                isLoading = false
            }
        )
    }

    when {
        isLoading -> {
            CircularProgressIndicator()
        }
        replacement == null -> {
            onBack()
        }
        else -> {
            ProcessReplacementScreen(
                replacement = replacement!!,
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