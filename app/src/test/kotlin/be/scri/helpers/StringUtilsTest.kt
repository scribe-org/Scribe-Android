// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StringUtilsTest {
    @Test
    fun isWordCapitalized_returnsTrue_forCapitalizedWord() {
        assertTrue(StringUtils.isWordCapitalized("Hello"))
    }

    @Test
    fun isWordCapitalized_returnsFalse_forLowercaseWord() {
        assertFalse(StringUtils.isWordCapitalized("hello"))
    }

    @Test
    fun isWordCapitalized_returnsFalse_forEmptyString() {
        assertFalse(StringUtils.isWordCapitalized(""))
    }

    @Test
    fun isWordCapitalized_returnsTrue_forSingleUppercaseLetter() {
        assertTrue(StringUtils.isWordCapitalized("A"))
    }

    @Test
    fun isWordCapitalized_returnsFalse_forSingleLowercaseLetter() {
        assertFalse(StringUtils.isWordCapitalized("a"))
    }

    @Test
    fun isWordCapitalized_returnsFalse_forNumberStartingString() {
        assertFalse(StringUtils.isWordCapitalized("1hello"))
    }
}
