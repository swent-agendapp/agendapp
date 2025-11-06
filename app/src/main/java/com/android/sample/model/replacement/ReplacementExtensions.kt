package com.android.sample.model.replacement

/** Return only the replacements that are currently pending */
fun List<Replacement>.pendingReplacements(): List<Replacement> = filter {
  it.status == ReplacementStatus.Pending
}
