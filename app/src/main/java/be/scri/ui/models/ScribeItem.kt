/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/*
 * Copyright (C) 2024 Scribe-Android
 *
 * This file is part of Scribe-Android and is licensed under the
 * GNU General Public License, version 3.
 * See LICENSE for more information.
 */
/**
 * Class of methods to manage Scribe's behaviors.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package be.scri.ui.models

import androidx.annotation.DrawableRes

sealed class ScribeItem(
    open val title: String,
    open val desc: String?,
) {
    data class ClickableItem(
        override val title: String,
        override val desc: String? = null,
        val action: () -> Unit,
    ) : ScribeItem(title, desc)

    data class SwitchItem(
        override val title: String,
        override val desc: String,
        val state: Boolean,
        val onToggle: (Boolean) -> Unit,
    ) : ScribeItem(title, desc)

    data class ExternalLinkItem(
        override val title: String,
        override val desc: String? = null,
        @DrawableRes val leadingIcon: Int,
        @DrawableRes val trailingIcon: Int,
        val url: String?,
        val onClick: () -> Unit,
    ) : ScribeItem(title, desc)

    data class CustomItem(
        override val title: String,
        override val desc: String,
        val customAction: (Any?) -> Unit,
    ) : ScribeItem(title, desc)
}
