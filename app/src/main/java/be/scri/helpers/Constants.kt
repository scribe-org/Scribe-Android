/**
 * This file contains the implementation for Constants.
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
package be.scri.helpers

const val SHIFT_OFF = 0
const val SHIFT_ON_ONE_CHAR = 1
const val SHIFT_ON_PERMANENT = 2

// Limit the count of alternative characters that show up at long pressing a key.
const val MAX_KEYS_PER_MINI_ROW = 9

// Shared preferences.
const val VIBRATE_ON_KEYPRESS = "vibrate_on_keypress"
const val SHOW_POPUP_ON_KEYPRESS = "show_popup_on_keypress"
const val DARK_THEME = "dark_theme"
const val PERIOD_ON_DOUBLE_TAP = "period_on_double_tap"
