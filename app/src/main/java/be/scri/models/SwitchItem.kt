// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * The switch item model class used in the application.
 */

package be.scri.models

/**
 * Base class for defining UI items in the application.
 */
sealed class Item

/**
 * Model representing a switch-based item with optional actions.
 */
data class SwitchItem(
    val title: String,
    val description: String? = null,
    var isChecked: Boolean,
    val action: (() -> Unit)? = null,
    val action2: (() -> Unit)? = null,
) : Item()
