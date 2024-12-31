/**
 * The item view model class used in the application.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
