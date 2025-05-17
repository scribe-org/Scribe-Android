// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * The item view model class used in the application.
 */

package be.scri.models

import android.app.Activity
import androidx.annotation.StringRes

/**
 * ViewModel representing an item with optional actions and associated resources like images and URLs.
 */
data class ItemsViewModel(
    val image: Int,
    val text: Text,
    val image2: Int,
    val url: String? = null,
    val activity: Class<out Activity>? = null,
    val action: (() -> Unit)? = null,
) : Item() {
    /**
     * ViewModel representing an item with optional actions and associated resources like images and URLs.
     */
    class Text(
        @StringRes
        val resId: Int,
        vararg val formatArgs: Any,
    )
}
