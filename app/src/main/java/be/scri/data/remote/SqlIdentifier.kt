// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.data.remote

internal object SqlIdentifier {
    private val VALID_IDENTIFIER = Regex("^[A-Za-z_][A-Za-z0-9_]*$")

    fun isValid(name: String): Boolean = VALID_IDENTIFIER.matches(name)

    fun quote(name: String): String = "\"$name\""
}
