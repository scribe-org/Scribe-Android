// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

/**
 * Validates SQLite identifier names (column and table names).
 * Protects against SQL injection when identifiers from remote data contracts
 * are dynamically interpolated into database queries.
 */
object SqlIdentifier {
    private const val MAX_IDENTIFIER_LENGTH = 128
    private val IDENTIFIER_REGEX = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")

    /**
     * Checks if [name] is a valid SQLite column/table identifier.
     * Must be non-blank, <= 128 characters, and contain only alphanumeric characters
     * and underscores, starting with a letter or underscore.
     *
     * @param name The candidate identifier name to validate.
     * @return True if valid, false otherwise.
     */
    fun isValid(name: String): Boolean = name.isNotBlank() && name.length <= MAX_IDENTIFIER_LENGTH && IDENTIFIER_REGEX.matches(name)
}
