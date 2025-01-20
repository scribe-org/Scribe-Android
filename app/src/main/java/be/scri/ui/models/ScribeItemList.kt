// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * A class defining lists of ScribeItem elements.
 */

package be.scri.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class ScribeItemList(
    val items: List<ScribeItem>,
)
