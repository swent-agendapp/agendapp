package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.replacement.route.ReplacementPendingRoute
import com.android.sample.ui.replacement.route.ReplacementPendingRouteTestTags
import com.android.sample.ui.theme.SampleAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FakeReplacementPendingViewModel(
    initialState: ReplacementPendingUiState,
) : ReplacementPendingContract {

  private val _uiState = MutableStateFlow(initialState)
  override val uiState: StateFlow<ReplacementPendingUiState> = _uiState

  var refreshCalled = false
    private set

  override fun refresh() {
    refreshCalled = true
  }

  fun updateState(newState: ReplacementPendingUiState) {
    _uiState.value = newState
  }
}

class ReplacementPendingRouteTest {

  @get:Rule val compose = createComposeRule()

  @Test
  fun whenLoading_showsCircularProgressIndicator() {
    val fakeVm =
        FakeReplacementPendingViewModel(
            initialState = ReplacementPendingUiState(isLoading = true),
        )

    compose.setContent {
      SampleAppTheme {
        ReplacementPendingRoute(
            onProcessReplacement = {},
            onBack = {},
            viewModel = fakeVm,
        )
      }
    }

    compose.onNodeWithTag(ReplacementPendingRouteTestTags.LOADING).assertIsDisplayed()

    compose.runOnIdle { assertTrue(fakeVm.refreshCalled) }
  }

  @Test
  fun whenNotLoading_showsPendingListScreen() {
    val replacements = getMockReplacements()

    val fakeVm =
        FakeReplacementPendingViewModel(
            initialState =
                ReplacementPendingUiState(
                    isLoading = false,
                    toProcess = replacements,
                    waitingForAnswer = emptyList(),
                ),
        )

    compose.setContent {
      SampleAppTheme {
        ReplacementPendingRoute(
            onProcessReplacement = {},
            onBack = {},
            viewModel = fakeVm,
        )
      }
    }

    compose.onNodeWithTag(ReplacementPendingRouteTestTags.LOADING).assertDoesNotExist()

    compose
        .onNodeWithTag(ReplacementPendingTestTags.SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
