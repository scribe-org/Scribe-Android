// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The item view model class used in the application.
 */

package be.scri.models

import android.app.Activity
import androidx.annotation.StringRes

data class ItemsViewModel(
    val image: Int,
    val text: Text,
    val image2: Int,
    val url: String? = null,
    val activity: Class<out Activity>? = null,
    val action: (() -> Unit)? = null,
) : Item() {
    class Text(
        @StringRes
        val resId: Int,
        vararg val formatArgs: Any,
    )
}
