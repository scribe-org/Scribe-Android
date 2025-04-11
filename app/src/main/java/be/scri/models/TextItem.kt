// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * The text item model class used in the application.
 */

package be.scri.models

import androidx.fragment.app.Fragment

data class TextItem(
    val text: Int,
    val image: Int,
    val description: String? = null,
    val action: (() -> Unit)? = null,
    val language: String? = null,
    val fragment: Fragment? = null,
    val fragmentTag: String? = null,
) : Item()
