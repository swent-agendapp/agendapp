package com.android.sample.model.replacement

import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.pendingAdminReplacements
import com.android.sample.model.replacement.waitingForAnswerAndDeclinedReplacements
import com.google.common.truth.Truth
import org.junit.Test

class MockReplacementsTest {

  @Test
  fun getMockReplacements_containsPendingAndNonPending() {
    val replacements = getMockReplacements()

    val pending = replacements.pendingAdminReplacements()
    val nonPending = replacements.filter { it.status != ReplacementStatus.ToProcess }

    Truth.assertThat(replacements).isNotEmpty()
    Truth.assertThat(pending).isNotEmpty()
    Truth.assertThat(nonPending).isNotEmpty()
  }

  @Test
  fun getMockReplacements_containsWaitingAndDeclinedForWaitingSection() {
    val replacements = getMockReplacements()

    val waitingAndDeclined = replacements.waitingForAnswerAndDeclinedReplacements()

    Truth.assertThat(waitingAndDeclined).isNotEmpty()
    Truth.assertThat(waitingAndDeclined.any { it.status == ReplacementStatus.WaitingForAnswer })
        .isTrue()
    Truth.assertThat(waitingAndDeclined.any { it.status == ReplacementStatus.Declined }).isTrue()
  }
}
