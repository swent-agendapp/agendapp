package com.android.sample.model.replacement

import com.android.sample.model.replacement.mockData.getMockReplacements
import com.google.common.truth.Truth
import org.junit.Test

class MockReplacementsTest {

  @Test
  fun getMockReplacements_containsPendingAndNonPending() {
    val replacements = getMockReplacements()

    val pending = replacements.pendingReplacements()
    val nonPending = replacements.filter { it.status != ReplacementStatus.Pending }

    Truth.assertThat(replacements).isNotEmpty()
    Truth.assertThat(pending).isNotEmpty()
    Truth.assertThat(nonPending).isNotEmpty()
  }
}
