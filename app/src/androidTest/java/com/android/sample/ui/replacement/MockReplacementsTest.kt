package com.android.sample.ui.replacement

import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.model.replacement.pendingReplacements
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MockReplacementsTest {

  @Test
  fun getMockReplacements_containsPendingAndNonPending() {
    val replacements = getMockReplacements()

    val pending = replacements.pendingReplacements()
    val nonPending = replacements.filter { it.status != ReplacementStatus.Pending }

    assertThat(replacements).isNotEmpty()
    assertThat(pending).isNotEmpty()
    assertThat(nonPending).isNotEmpty()
  }
}
