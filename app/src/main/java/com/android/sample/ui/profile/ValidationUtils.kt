package com.android.sample.ui.profile

import android.util.Patterns

/** Utility validation functions */
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPhone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length >= 7 // basic length check
}