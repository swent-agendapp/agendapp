package com.android.sample.ui.profile

import android.util.Patterns
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ValidationUtilsTest {

    @Test
    fun validEmails_shouldPass() {
        val validEmails = listOf(
            "user@example.com",
            "user.name@domain.co",
            "user_name@domain.io",
            "user+alias@sub.domain.com",
            "firstname.lastname@company.org",
            "u@a.co"
        )
        validEmails.forEach {
            assertTrue("Expected valid email: $it", Patterns.EMAIL_ADDRESS.matcher(it).matches())
        }
    }

    @Test
    fun invalidEmails_shouldFail() {
        val invalidEmails = listOf(
            "",
            "plainaddress",
            "@missingusername.com",
            "username@.com",
            "username@domain",
            "username@domain,com",
            "username@domain..com",
            "username@ domain.com"
        )
        invalidEmails.forEach {
            assertFalse("Expected invalid email: $it", Patterns.EMAIL_ADDRESS.matcher(it).matches())
        }
    }
    @Test
    fun validPhones_shouldPass() {
        val validPhones = listOf(
            // 🇫🇷 France
            "+33 6 12 34 56 78",
            "06 12 34 56 78",
            "0033 6 12 34 56 78",
            "+33 (0)1 23 45 67 89",

            // 🇩🇪 Germany
            "+49 30 123456",
            "+49 (0) 89 1234 5678",
            "0049 171 1234567",
            "+49-40-123-4567",

            // 🇬🇧 United Kingdom
            "+44 20 7946 0958",
            "020 7946 0958",
            "+44 (0) 161 496 0000",
            "0044 161 496 0000",

            // 🇮🇹 Italy
            "+39 06 6982",
            "+39 347 123 4567",
            "0039 06 1234567",
            "+39 (06) 1234-5678",

            // 🇪🇸 Spain
            "+34 612 34 56 78",
            "0034 912 34 56 78",
            "+34-612-345-678",
            "612 34 56 78",

            // 🇳🇱 Netherlands
            "+31 6 12 34 56 78",
            "0031 6 12 34 56 78",
            "+31 (0)20 123 4567",
            "06-12345678",

            // 🇧🇪 Belgium
            "+32 2 555 12 12",
            "+32 (0) 472 12 34 56",
            "0032 2 555 12 12",

            // 🇨🇭 Switzerland
            "+41 44 668 18 00",
            "+41 (0)44 668 18 00",
            "0041 44 668 18 00",

            // 🇸🇪 Sweden
            "+46 8 123 456 78",
            "+46 (0)8 123 456 78",
            "0046 8 123 456 78",

            // 🇳🇴 Norway
            "+47 401 23 456",
            "0047 401 23 456",

            // 🇩🇰 Denmark
            "+45 12 34 56 78",
            "0045 12 34 56 78",

            // 🇫🇮 Finland
            "+358 40 123 4567",
            "00358 40 123 4567",

            // 🇵🇱 Poland
            "+48 601 234 567",
            "0048 601 234 567",
            "+48 (22) 123 45 67",

            // 🇵🇹 Portugal
            "+351 91 234 5678",
            "00351 21 234 5678",

            // 🇮🇪 Ireland
            "+353 86 123 4567",
            "00353 1 234 5678"
        )
        validPhones.forEach {
            assertTrue("Expected valid phone: $it", Patterns.PHONE.matcher(it).matches())
        }
    }
    @Test
    fun invalidPhones_shouldFail() {
        val invalidPhones = listOf(
            "", // empty
            "abcde", // letters
            "+1", // too short
            "12", // too short
            "+99999999999999999999", // too long
            "555", // too short
            "1234abcd567", // letters mixed in
            "+-+--+", // only symbols

            // European-like invalids
            "+33 6 12 34 56 7890 12", // too many digits
            "+33 6 12", // too few digits
            "+33) 6 12 34 56 78", // misplaced parenthesis
            "+44 (0 20 7946 0958", // unclosed parenthesis
            "+44-20-7946--0958", // double dash
            "0044207946O958", // letter O instead of zero
            "+49 (0) 89 12a4 5678", // contains letter
            "+31 ++6 12 34 56 78", // double plus
            "0031-()-12345678", // wrong symbols
            "+351-91-234-5678901234", // too long
            "++44 20 7946 0958", // extra +
            "+44 20 7946 095@", // symbol at end
            "+44(20)7946_0958", // underscore
            "+33 06-12-34--56-78" // multiple double dashes
        )

        invalidPhones.forEach {
            assertFalse("Expected invalid phone: $it", isValidPhone(it))
        }
    }
}