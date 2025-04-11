// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Constants that are used throughout helper classes and objects.
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
const val PERIOD_AND_COMMA = "period_and_comma"
const val DISABLE_ACCENT_CHARACTER = "disable_accent_character"
const val EMOJI_SUGGESTIONS = "emoji_suggestions"
const val TRANSLATION_SOURCE = "translation_source"
