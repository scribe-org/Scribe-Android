// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.models

import androidx.compose.runtime.Immutable

/**
 * A class defining lists of ScribeItem elements.
 */
@Immutable
data class ScribeItemList(
    val items: List<ScribeItem>,
)
