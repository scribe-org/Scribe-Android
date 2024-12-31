/**
 * The text item model class used in the application.
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
