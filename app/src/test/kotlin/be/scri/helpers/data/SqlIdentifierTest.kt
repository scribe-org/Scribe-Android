// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SqlIdentifierTest {
    @Test
    fun isValid_acceptsValidColumnNames() {
        assertTrue(SqlIdentifier.isValid("gender"))
        assertTrue(SqlIdentifier.isValid("nominativeSingular"))
        assertTrue(SqlIdentifier.isValid("nominative_plural"))
        assertTrue(SqlIdentifier.isValid("_id"))
        assertTrue(SqlIdentifier.isValid("simplePresent1"))
        assertTrue(SqlIdentifier.isValid("columnName"))
    }

    @Test
    fun isValid_rejectsSqlInjectionAndSpecialCharacters() {
        assertFalse(SqlIdentifier.isValid("col; DROP TABLE nouns;--"))
        assertFalse(SqlIdentifier.isValid("col' OR '1'='1"))
        assertFalse(SqlIdentifier.isValid("col\" OR \"1\"=\"1"))
        assertFalse(SqlIdentifier.isValid("col`"))
        assertFalse(SqlIdentifier.isValid("col-name"))
        assertFalse(SqlIdentifier.isValid("col name"))
        assertFalse(SqlIdentifier.isValid("col(1)"))
        assertFalse(SqlIdentifier.isValid("col[0]"))
        assertFalse(SqlIdentifier.isValid("col/name"))
    }

    @Test
    fun isValid_rejectsEmptyOrBlank() {
        assertFalse(SqlIdentifier.isValid(""))
        assertFalse(SqlIdentifier.isValid("   "))
    }

    @Test
    fun isValid_rejectsStartingWithDigit() {
        assertFalse(SqlIdentifier.isValid("1column"))
        assertFalse(SqlIdentifier.isValid("0"))
    }

    @Test
    fun isValid_rejectsOverlyLongIdentifiers() {
        val longName = "a".repeat(129)
        assertFalse(SqlIdentifier.isValid(longName))
        val maxAllowed = "a".repeat(128)
        assertTrue(SqlIdentifier.isValid(maxAllowed))
    }
}
