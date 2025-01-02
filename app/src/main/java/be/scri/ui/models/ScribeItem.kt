/**
 * A class defining different types of items used in the application interface.
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

package be.scri.ui.models

import androidx.annotation.DrawableRes

sealed class ScribeItem(
    open val title: Int,
    open val desc: Int?,
) {
    data class ClickableItem(
        override val title: Int,
        override val desc: Int? = null,
        val action: () -> Unit,
    ) : ScribeItem(title, desc)

    data class SwitchItem(
        override val title: Int,
        override val desc: Int,
        val state: Boolean,
        val onToggle: (Boolean) -> Unit,
    ) : ScribeItem(title, desc)

    data class ExternalLinkItem(
        override val title: Int,
        override val desc: Int? = null,
        @DrawableRes val leadingIcon: Int,
        @DrawableRes val trailingIcon: Int,
        val url: String?,
        val onClick: () -> Unit,
    ) : ScribeItem(title, desc)

    data class CustomItem(
        override val title: Int,
        override val desc: Int,
        val customAction: (Any?) -> Unit,
    ) : ScribeItem(title, desc)
}
