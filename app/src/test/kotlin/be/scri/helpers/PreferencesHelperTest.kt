// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Testing for PreferencesHelper.
 */

package be.scri.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PreferencesHelperTest {
    @Test
    fun testGetLanguageSpecificPreferenceKey() {
        assertEquals("KEY_English", PreferencesHelper.getLanguageSpecificPreferenceKey("KEY", "English"))
    }
}
