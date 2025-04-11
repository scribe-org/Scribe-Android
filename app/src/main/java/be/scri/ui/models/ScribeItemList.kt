// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * A class defining lists of ScribeItem elements.
 */

package be.scri.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class ScribeItemList(
    val items: List<ScribeItem>,
)
