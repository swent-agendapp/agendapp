package com.android.sample.model.profile

import com.android.sample.ui.profile.isValidEmail
import com.android.sample.ui.profile.isValidPhone
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import kotlin.test.Test
import org.junit.Assert.*


class ProfileScreenLogicTest {
        // --- Email validation tests ---
        @Test
        fun validEmails_shouldPass() {
            val validEmails =
                listOf(
                    "user@example.com",
                    "user.name@domain.co",
                    "user_name@domain.io",
                    "user+alias@sub.domain.com",
                    "firstname.lastname@company.org",
                    "u@a.co",
                )

            validEmails.forEach {
                assertTrue("Expected valid email: $it", isValidEmail(it))
            }
        }

        @Test
        fun invalidEmails_shouldFail() {
            val invalidEmails =
                listOf(
                    "",
                    "plainaddress",
                    "@missingusername.com",
                    "username@.com",
                    "username@domain",
                    "username@domain,com",
                    "username@domain..com",
                    "username@ domain.com",
                )

            invalidEmails.forEach {
                assertFalse("Expected invalid email: $it", isValidEmail(it))
            }
        }

        // --- Phone validation tests ---
        @Test
        fun validPhones_shouldPass() {
            val validPhones =
                listOf(
                    "+1 (555) 123-4567", // US
                    "+44 20 7946 0958", // UK
                    "+33 1 23 45 67 89", // France
                    "+49 30 123456", // Germany
                    "+91-9876543210", // India
                    "+81 3-1234-5678", // Japan
                    "+86 10 8888 9999", // China
                    "+61 2 9374 4000", // Australia
                    "0033 6 12 34 56 78", // France alternate format
                    "06 12 34 56 78", // French local
                    "5551234567", // simple digits
                )

            validPhones.forEach {
                assertTrue("Expected valid phone: $it", isValidPhone(it))
            }
        }

        @Test
        fun invalidPhones_shouldFail() {
            val invalidPhones =
                listOf(
                    "",
                    "abcde",
                    "+1",
                    "12",
                    "+99999999999999999999", // too long
                    "555", // too short
                    "1234abcd567", // contains letters
                    "+-+--+", // only symbols
                )

            invalidPhones.forEach {
                assertFalse("Expected invalid phone: $it", isValidPhone(it))
            }
        }
    }
