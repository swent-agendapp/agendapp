package com.android.sample.model.replacement

/** Return only the replacements that are currently pending */
fun List<Replacement>.pendingReplacements(): List<Replacement> = filter {
  it.status == ReplacementStatus.ToProcess || it.status == ReplacementStatus.WaitingForAnswer
}

/** Replacements the admin still has to process (no substitute chosen yet) */
fun List<Replacement>.toProcessReplacements(): List<Replacement> = filter {
  it.status == ReplacementStatus.ToProcess
}

/** Replacements where we are waiting for a substitute's answer */
fun List<Replacement>.waitingForAnswerReplacements(): List<Replacement> = filter {
  it.status == ReplacementStatus.WaitingForAnswer
}

fun List<Replacement>.waitingForAnswerAndDeclinedReplacements(): List<Replacement> = filter {
  it.status == ReplacementStatus.WaitingForAnswer || it.status == ReplacementStatus.Declined
}
