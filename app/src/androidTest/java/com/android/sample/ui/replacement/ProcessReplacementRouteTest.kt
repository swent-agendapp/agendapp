package com.android.sample.ui.replacement

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.model.authentication.User
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.mockData.getMockUsers
import com.android.sample.ui.replacement.mainPage.ReplacementEmployeeActions
import com.android.sample.ui.replacement.route.ProcessReplacementRoute
import com.android.sample.ui.replacement.route.ProcessReplacementRouteTestTags
import com.android.sample.ui.theme.SampleAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Asisted by IA
class FakeReplacementEmployeeViewModel(
    var replacementToReturn: Replacement? = null,
    var usersToReturn: List<User> = listOf(),
    var autoCompleteLoad: Boolean = true,
) : ReplacementEmployeeActions {

  var lastLoadedReplacementId: String? = null
    private set

  var lastSendRequestsReplacementId: String? = null
    private set

  var lastSelectedSubstitutes: List<User> = emptyList()
    private set

  override fun loadReplacementForProcessing(
      replacementId: String,
      onResult: (replacement: Replacement?, users: List<User>) -> Unit
  ) {
    lastLoadedReplacementId = replacementId
    if (autoCompleteLoad) {
      onResult(replacementToReturn, usersToReturn)
    }
  }

  override fun sendRequestsForPendingReplacement(
      replacementId: String,
      selectedSubstitutes: List<User>,
      onFinished: () -> Unit
  ) {
    lastSendRequestsReplacementId = replacementId
    lastSelectedSubstitutes = selectedSubstitutes
    onFinished()
  }
}

class ProcessReplacementRouteTest {

  @get:Rule val compose = createComposeRule()

  private val replacement = getMockReplacements().first()
  private val users = getMockUsers()

  @Test
  fun loading_showsCircularProgressIndicator() {
    val fakeVm =
        FakeReplacementEmployeeViewModel(
            replacementToReturn = null,
            autoCompleteLoad = false,
        )

    compose.setContent {
      SampleAppTheme {
        ProcessReplacementRoute(
            replacementId = "rep123",
            onFinished = {},
            onBack = {},
            viewModel = fakeVm,
        )
      }
    }

    compose.onNodeWithTag(ProcessReplacementRouteTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun whenReplacementIsNull_callsOnBack() {
    val fakeVm =
        FakeReplacementEmployeeViewModel(
            replacementToReturn = null,
            autoCompleteLoad = true,
        )

    var backCalled = false

    compose.setContent {
      SampleAppTheme {
        ProcessReplacementRoute(
            replacementId = "rep_not_found",
            onFinished = {},
            onBack = { backCalled = true },
            viewModel = fakeVm,
        )
      }
    }

    compose.waitForIdle()

    assertTrue(backCalled)
    assertEquals("rep_not_found", fakeVm.lastLoadedReplacementId)
  }

  @Test
  fun whenReplacementLoaded_showsProcessReplacementScreen() {
    val fakeVm =
        FakeReplacementEmployeeViewModel(
            replacementToReturn = replacement,
            autoCompleteLoad = true,
        )

    compose.setContent {
      SampleAppTheme {
        ProcessReplacementRoute(
            replacementId = "rep_ok",
            onFinished = {},
            onBack = {},
            viewModel = fakeVm,
        )
      }
    }

    compose.waitForIdle()

    compose.onNodeWithTag(ProcessReplacementRouteTestTags.LOADING_INDICATOR).assertDoesNotExist()

    compose
        .onNodeWithTag(ProcessReplacementTestTags.ROOT, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun sendRequests_callsViewModel_andOnFinished() {
    val fakeVm =
        FakeReplacementEmployeeViewModel(
            replacementToReturn = replacement,
            usersToReturn = users,
            autoCompleteLoad = true,
        )

    var finishedCalled = false

    compose.setContent {
      SampleAppTheme {
        ProcessReplacementRoute(
            replacementId = "rep_integration",
            onFinished = { finishedCalled = true },
            onBack = {},
            viewModel = fakeVm,
        )
      }
    }

    compose.waitForIdle()
    compose
        .onNodeWithTag(
            ProcessReplacementTestTags.memberTag(fakeVm.usersToReturn.first { it.id == "emilien" }),
            useUnmergedTree = true)
        .performClick()

    compose.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).performClick()

    compose.waitForIdle()

    assertEquals("rep_integration", fakeVm.lastSendRequestsReplacementId)
    assertEquals(1, fakeVm.lastSelectedSubstitutes.size)
    assertTrue(finishedCalled)
  }
}
