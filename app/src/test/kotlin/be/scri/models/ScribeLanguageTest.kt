// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ScribeLanguageTest {
    @Test
    fun defaultLayout_isCorrect_forAllLanguages() {
        assertEquals("QWERTY", ScribeLanguage.ENGLISH.defaultLayout)
        assertEquals("AZERTY", ScribeLanguage.FRENCH.defaultLayout)
        assertEquals("QWERTZ", ScribeLanguage.GERMAN.defaultLayout)
        assertEquals("QWERTY", ScribeLanguage.ITALIAN.defaultLayout)
        assertEquals("QWERTY", ScribeLanguage.PORTUGUESE.defaultLayout)
        assertEquals("ЙЦУКЕН", ScribeLanguage.RUSSIAN.defaultLayout)
        assertEquals("QWERTY", ScribeLanguage.SPANISH.defaultLayout)
        assertEquals("QWERTY", ScribeLanguage.SWEDISH.defaultLayout)
    }

    @Test
    fun fromDisplayName_resolvesDefaultLayoutCorrectly() {
        assertEquals("QWERTZ", ScribeLanguage.fromDisplayName("German").defaultLayout)
        assertEquals("AZERTY", ScribeLanguage.fromDisplayName("French").defaultLayout)
        assertEquals("ЙЦУКЕН", ScribeLanguage.fromDisplayName("Russian").defaultLayout)
        assertEquals("QWERTY", ScribeLanguage.fromDisplayName("Spanish").defaultLayout)
        assertEquals("QWERTY", ScribeLanguage.fromDisplayName("Unknown").defaultLayout)
    }
}

