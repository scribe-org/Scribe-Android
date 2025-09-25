// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.app.UiModeManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.UI_MODE_SERVICE
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

/**
 * A helper to facilitate setting preferences for individual language keyboards.
 */
@Suppress("TooManyFunctions")
object PreferencesHelper {
    const val SCRIBE_PREFS = "app_preferences"
    private const val PERIOD_ON_DOUBLE_TAP = "period_on_double_tap"
    private const val VIBRATE_ON_KEYPRESS = "vibrate_on_keypress"
    private const val SOUND_ON_KEYPRESS = "sound_on_keypress"
    private const val SHOW_POPUP_ON_KEYPRESS = "show_popup_on_keypress"
    private const val PERIOD_AND_COMMA = "period_and_comma"
    private const val TRANSLATION_SOURCE = "translation_source"
    private const val EMOJI_SUGGESTIONS = "emoji_suggestions"
    private const val DISABLE_ACCENT_CHARACTER = "disable_accent_character"
    private const val WORD_BY_WORD_DELETION = "word_by_word_deletion"
    private const val DEFAULT_CURRENCY = "default_currency"
    private const val HOLD_FOR_ALT_KEYS = "hold_for_alt_keys"

    /**
     * Sets the translation source language for a given language.
     *
     * @param context The application context.
     * @param language The language for which to set the translation source.
     * @param translationSource The source language to set for translation.
     */
    fun setTranslationSourceLanguage(
        context: Context,
        language: String,
        translationSource: String,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit { putString(getLanguageSpecificPreferenceKey(TRANSLATION_SOURCE, language), translationSource) }
    }

    /**
     * Retrieves the translation source language for a given language.
     *
     * @param context The application context.
     * @param language The language for which to get the translation source.
     * @return The translation source language.
     */
    fun getTranslationSourceLanguage(
        context: Context,
        language: String,
    ): String {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        return sharedPref.getString(
            getLanguageSpecificPreferenceKey(TRANSLATION_SOURCE, language),
            "English",
        ) ?: "English"
    }

    /**
     * Generates a language-specific preference key for storing preferences.
     *
     * @param key The base key.
     * @param language The language for which to generate the preference key.
     * @return The generated language-specific preference key.
     */
    fun getLanguageSpecificPreferenceKey(
        key: String,
        language: String,
    ): String = "${key}_$language"

    /**
     * Sets the preference for enabling or disabling the period on spacebar double tap.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldEnablePeriodOnSpaceBarDoubleTap Whether to enable or disable the feature.
     */
    fun setPeriodOnSpaceBarDoubleTapPreference(
        context: Context,
        language: String,
        shouldEnablePeriodOnSpaceBarDoubleTap: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(
                getLanguageSpecificPreferenceKey(PERIOD_ON_DOUBLE_TAP, language),
                shouldEnablePeriodOnSpaceBarDoubleTap,
            )
        }
        Toast
            .makeText(
                context,
                "$language Period on Double Tap of Space Bar " +
                    if (shouldEnablePeriodOnSpaceBarDoubleTap) "on" else "off",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for disabling or enabling accent characters for a language.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldDisableAccentCharacter Whether to disable or enable accent characters.
     */
    fun setAccentCharacterPreference(
        context: Context,
        language: String,
        shouldDisableAccentCharacter: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(
                getLanguageSpecificPreferenceKey(DISABLE_ACCENT_CHARACTER, language),
                shouldDisableAccentCharacter,
            )
        }
        Toast
            .makeText(
                context,
                "$language Accent Characters " +
                    if (shouldDisableAccentCharacter) "off" else "on",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for enabling or disabling emoji auto-suggestions for a language.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldShowEmojiSuggestions Whether to show or hide emoji suggestions.
     */
    fun setEmojiAutoSuggestionsPreference(
        context: Context,
        language: String,
        shouldShowEmojiSuggestions: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(getLanguageSpecificPreferenceKey(EMOJI_SUGGESTIONS, language), shouldShowEmojiSuggestions)
        }
        Toast
            .makeText(
                context,
                "$language Emoji Autosuggestions " +
                    if (shouldShowEmojiSuggestions) "on" else "off",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for enabling or disabling period and comma on the ABC keyboard layout for a language.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldEnablePeriodAndComma Whether to enable or disable the period and comma feature.
     */
    fun setCommaAndPeriodPreference(
        context: Context,
        language: String,
        shouldEnablePeriodAndComma: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language), shouldEnablePeriodAndComma)
        }
        Toast
            .makeText(
                context,
                "$language period and comma on ABC " +
                    if (shouldEnablePeriodAndComma) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for enabling or disabling vibration on keypress for a language.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldVibrateOnKeypress Whether to enable or disable vibration on keypress.
     */
    fun setVibrateOnKeypress(
        context: Context,
        language: String,
        shouldVibrateOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(getLanguageSpecificPreferenceKey(VIBRATE_ON_KEYPRESS, language), shouldVibrateOnKeypress)
        }
        Toast
            .makeText(
                context,
                "$language vibrate on key press " +
                    if (shouldVibrateOnKeypress) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    fun setSoundOnKeypress(
        context: Context,
        language: String,
        shouldSoundOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(getLanguageSpecificPreferenceKey(SOUND_ON_KEYPRESS, language), shouldSoundOnKeypress)
        }
        Toast
            .makeText(
                context,
                "$language sound on key press " +
                    if (shouldSoundOnKeypress) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for showing or hiding the popup on keypress for a language.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldShowPopupOnKeypress Whether to show or hide the popup on keypress.
     */
    fun setShowPopupOnKeypress(
        context: Context,
        language: String,
        shouldShowPopupOnKeypress: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(getLanguageSpecificPreferenceKey(SHOW_POPUP_ON_KEYPRESS, language), shouldShowPopupOnKeypress)
        }
        Toast
            .makeText(
                context,
                "$language PopUp on Keypress " +
                    if (shouldShowPopupOnKeypress) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for enabling or disabling word-by-word deletion for a language.
     *
     * @param context The application context.
     * @param language The language for which to set the preference.
     * @param shouldEnableWordByWordDeletion Whether to enable or disable word-by-word deletion.
     */
    fun setWordByWordDeletionPreference(
        context: Context,
        language: String,
        shouldEnableWordByWordDeletion: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(
                getLanguageSpecificPreferenceKey(WORD_BY_WORD_DELETION, language),
                shouldEnableWordByWordDeletion,
            )
        }
        Toast
            .makeText(
                context,
                "$language Word by Word Deletion " +
                    if (shouldEnableWordByWordDeletion) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Sets the preference for enabling or disabling light/dark mode.
     *
     * @param context The application context.
     * @param darkMode Whether to enable or disable dark mode.
     */
    fun setLightDarkModePreference(
        context: Context,
        darkMode: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean("dark_mode", darkMode)
        }
    }

    /**
     * Retrieves the user's dark mode preference.
     *
     * @param context The application context.
     * @return The dark mode setting as an integer value (AppCompatDelegate.MODE_NIGHT_YES or MODE_NIGHT_NO).
     */
    fun getUserDarkModePreference(context: Context): Int {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        val uiModeManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager
        val isSystemDarkTheme = uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkTheme)
        if (!sharedPref.contains("dark_mode")) {
            setLightDarkModePreference(context, isUserDarkMode)
        }
        return if (isUserDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
    }

    /**
     * Retrieves whether accent characters are disabled for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if accent characters are disabled, false otherwise.
     */
    fun getIsAccentCharacterDisabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isAccentCharacterDisabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(DISABLE_ACCENT_CHARACTER, language), false)
        return isAccentCharacterDisabled
    }

    /**
     * Retrieves whether the preview feature is enabled for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if the preview feature is enabled, false otherwise.
     */
    fun isShowPopupOnKeypressEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isPreviewEnabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(SHOW_POPUP_ON_KEYPRESS, language), true)
        return isPreviewEnabled
    }

    /**
     * Retrieves whether vibration on keypress is enabled for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if vibration on keypress is enabled, false otherwise.
     */
    fun getIsVibrateEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isPreviewEnabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(VIBRATE_ON_KEYPRESS, language), true)
        return isPreviewEnabled
    }

    fun getIsSoundEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isSoundEnabled =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(SOUND_ON_KEYPRESS, language), false)
        return isSoundEnabled
    }

    /**
     * Retrieves whether period and comma are enabled on the ABC keyboard layout for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if period and comma are enabled, false otherwise.
     */
    fun getEnablePeriodAndCommaABC(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isEnabledPeriodAndCommaABC =
            sharedPref.getBoolean(getLanguageSpecificPreferenceKey(PERIOD_AND_COMMA, language), true)
        return isEnabledPeriodAndCommaABC
    }

    /**
     * Retrieves whether period after double tap on space enabled for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if double tap on space enabled for a given language, false otherwise.
     */
    fun getEnablePeriodOnSpaceBarDoubleTap(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        return sharedPref.getBoolean(getLanguageSpecificPreferenceKey(PERIOD_ON_DOUBLE_TAP, language), false)
    }

    /**
     * Retrieves whether dark mode is enabled based on user preferences or system settings.
     *
     * @param context The application context.
     * @return True if dark mode is enabled, false otherwise.
     */
    fun getIsDarkModeOrNot(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)
        return isUserDarkMode
    }

    /**
     * Retrieves whether emoji suggestions are enabled for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if emoji suggestions are enabled, false otherwise.
     */
    fun getIsEmojiSuggestionsEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean(getLanguageSpecificPreferenceKey(EMOJI_SUGGESTIONS, language), true)
        return isEnabled
    }

    /**
     * Retrieves whether word-by-word deletion is enabled for a given language.
     *
     * @param context The application context.
     * @param language The language for which to check the preference.
     * @return True if word-by-word deletion is enabled, false otherwise.
     */
    fun getIsWordByWordDeletionEnabled(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        val isEnabled = sharedPref.getBoolean(getLanguageSpecificPreferenceKey(WORD_BY_WORD_DELETION, language), false)
        return isEnabled
    }

    /**
     * Retrieves the preferred translation language for a given language.
     *
     * @param context The application context.
     * @param language The language for which to get the preferred translation language.
     *
     * @return The preferred translation language.
     * */
    fun getPreferredTranslationLanguage(
        context: Context,
        language: String,
    ): String? {
        val prefs = context.getSharedPreferences(SCRIBE_PREFS, MODE_PRIVATE)
        return prefs.getString(
            getLanguageSpecificPreferenceKey(TRANSLATION_SOURCE, language),
            "English",
        )
    }

    /**
     * Sets the default currency symbol preference for a specific language.
     *
     * @param context The application context.
     * @param language The language for which to set the currency preference.
     * @param currencyName The name of the currency (e.g., "Dollar", "Euro").
     */
    fun setDefaultCurrencySymbol(
        context: Context,
        language: String,
        currencyName: String,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putString(getLanguageSpecificPreferenceKey(DEFAULT_CURRENCY, language), currencyName)
        }
    }

    /**
     * Retrieves the default currency symbol preference for a specific language.
     *
     * @param context The application context.
     * @param language The language for which to get the currency preference.
     * @return The currency symbol (e.g., "$", "€").
     */
    fun getDefaultCurrencySymbol(
        context: Context,
        language: String,
    ): String {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        val currencyName =
            sharedPref.getString(
                getLanguageSpecificPreferenceKey(
                    DEFAULT_CURRENCY,
                    language,
                ),
                "Dollar",
            ) ?: "Dollar"

        // Map currency names to symbols.
        return when (currencyName) {
            "Dollar" -> "$"
            "Euro" -> "€"
            "Pound" -> "£"
            "Rouble" -> "₽"
            "Rupee" -> "₹"
            "Won" -> "₩"
            "Yen" -> "¥"
            else -> "$"
        }
    }

    /**
     * Retrieves the default currency name preference for a specific language.
     *
     * @param context The application context.
     * @param language The language for which to get the currency preference.
     * @return The currency name (e.g., "Dollar", "Euro").
     */
    fun getDefaultCurrencyName(
        context: Context,
        language: String,
    ): String {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        return sharedPref.getString(getLanguageSpecificPreferenceKey(DEFAULT_CURRENCY, language), "Dollar") ?: "Dollar"
    }

    /**
     * Sets the state of hold key style for a specific language.
     *
     * @param context The application context.
     * @param language The language for which to get the currency preference.
     * @param holdForAltKeys Whether to disable swipe selection on hold key.
     */
    fun setHoldKeyStyle(
        context: Context,
        language: String,
        holdForAltKeys: Boolean,
    ) {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        sharedPref.edit {
            putBoolean(getLanguageSpecificPreferenceKey(HOLD_FOR_ALT_KEYS, language), holdForAltKeys)
        }
        Toast
            .makeText(
                context,
                "$language hold for alternate characters " +
                    if (holdForAltKeys) "enabled" else "disabled",
                Toast.LENGTH_SHORT,
            ).show()
    }

    /**
     * Retrieves the state of hold key style for a specific language.
     *
     * @param context The application context.
     * @param language The language for which to get the currency preference.
     * @return The state of hold key style.
     */
    fun getHoldKeyStyle(
        context: Context,
        language: String,
    ): Boolean {
        val sharedPref = context.getSharedPreferences(SCRIBE_PREFS, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(getLanguageSpecificPreferenceKey(HOLD_FOR_ALT_KEYS, language), true)
    }
}
