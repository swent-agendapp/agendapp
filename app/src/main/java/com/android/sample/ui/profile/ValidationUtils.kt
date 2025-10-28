package com.android.sample.ui.profile

import android.util.Patterns

/** Utility validation functions */
fun isValidEmail(email: String): Boolean {
  return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPhone(phone: String): Boolean {
  // Basic Android structural check is really lenient, so we add extra rules
  val trimmed = phone.trim()

  // Basic Android structural check
  if (!Patterns.PHONE.matcher(trimmed).matches()) return false

  // Extra custom validation rules
  if ("--" in trimmed) return false // no double dashes
  if ("  " in trimmed) return false // no double spaces
  if (trimmed.count { it == '+' } > 1) return false // only one '+'
  if (trimmed.contains(Regex("""\(\s*\)"""))) return false // no empty parentheses

  // Enforce realistic digit count (7–15)
  val digits = trimmed.filter { it.isDigit() }
  return digits.length in 7..14
}
