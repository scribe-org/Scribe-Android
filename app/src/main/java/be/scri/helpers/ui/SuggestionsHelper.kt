// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.graphics.Color
import android.view.View
import android.widget.Button
import androidx.core.graphics.toColorInt
import be.scri.helpers.EmojiUtils.insertEmoji
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.Companion.SUGGESTION_SIZE
import be.scri.services.GeneralKeyboardIME.ScribeState

@Suppress("TooManyFunctions", "LargeClass")
class SuggestionsHelper(
    private val ime: GeneralKeyboardIME,
) {
    var emojiKeywords: HashMap<String, MutableList<String>>? = null
    private var emojiMaxKeywordLength: Int = 0

    internal fun setSuggestionButton(
        button: Button,
        text: String,
    ) {
        val isUserDarkMode = getIsDarkModeOrNot(ime.applicationContext)
        val textColor = if (isUserDarkMode) Color.WHITE else "#1E1E1E".toColorInt()
        button.text = text
        button.isAllCaps = false
        button.visibility = View.VISIBLE
        button.textSize = SUGGESTION_SIZE
        button.setOnClickListener(null)
        button.background = null
        button.setTextColor(textColor)
        button.setOnClickListener {
            ime.currentInputConnection?.commitText("$text ", 1)
            ime.moveToIdleState()
        }
    }

    internal fun handleWordSuggestions(
        wordSuggestions: List<String>? = null,
        hasLinguisticSuggestions: Boolean,
    ): Boolean {
        if (wordSuggestions.isNullOrEmpty()) {
            return false
        }
        val suggestion1 = wordSuggestions.getOrNull(0) ?: ""
        val suggestion2 = wordSuggestions.getOrNull(1) ?: ""
        val suggestion3 = wordSuggestions.getOrNull(2) ?: ""

        val emojiCount = ime.autoSuggestEmojis?.size ?: 0
        setSuggestionButton(ime.binding.conjugateBtn, suggestion1)
        when {
            hasLinguisticSuggestions && emojiCount != 0 -> {
                ime.updateButtonVisibility(true)
            }

            hasLinguisticSuggestions && emojiCount == 0 -> {
                setSuggestionButton(ime.binding.pluralBtn, suggestion2)
            }
            else -> {
                setSuggestionButton(ime.binding.translateBtn, suggestion2)
                setSuggestionButton(ime.binding.pluralBtn, suggestion3)
            }
        }
        return true
    }

    /**
     * Updates the text of the suggestion buttons, primarily for displaying emoji suggestions.
     * @param isAutoSuggestEnabled `true` if suggestions are active.
     * @param autoSuggestEmojis The list of emojis to display.
     */
    fun updateEmojiSuggestion(
        isAutoSuggestEnabled: Boolean,
        autoSuggestEmojis: MutableList<String>?,
    ) {
        if (ime.currentState != ScribeState.IDLE) return

        val tabletButtons = listOf(ime.binding.emojiBtnTablet1, ime.binding.emojiBtnTablet2, ime.binding.emojiBtnTablet3)
        val phoneButtons = listOf(ime.binding.emojiBtnPhone1, ime.binding.emojiBtnPhone2)

        if (isAutoSuggestEnabled && autoSuggestEmojis != null) {
            tabletButtons.forEachIndexed { index, button ->
                val emoji = autoSuggestEmojis.getOrNull(index) ?: ""
                button.text = emoji
                button.setOnClickListener {
                    if (emoji.isNotEmpty()) {
                        insertEmoji(
                            emoji,
                            ime.currentInputConnection,
                            emojiKeywords,
                            emojiMaxKeywordLength,
                        )
                    }
                }
            }

            phoneButtons.forEachIndexed { index, button ->
                val emoji = autoSuggestEmojis.getOrNull(index) ?: ""
                button.text = emoji
                button.setOnClickListener {
                    if (emoji.isNotEmpty()) {
                        insertEmoji(
                            emoji,
                            ime.currentInputConnection,
                            emojiKeywords,
                            emojiMaxKeywordLength,
                        )
                    }
                }
            }
        } else {
            (tabletButtons + phoneButtons).forEach { button ->
                button.text = ""
                button.setOnClickListener(null)
            }
        }
    }

    /**
     * Sets the default visibility for buttons when not in the `IDLE` state.
     * Hides all suggestion-related buttons.
     */
    internal fun setupDefaultButtonVisibility() {
        ime.binding.pluralBtn.visibility = View.VISIBLE
        ime.binding.emojiBtnPhone1.visibility = View.GONE
        ime.binding.emojiBtnPhone2.visibility = View.GONE
        ime.binding.emojiBtnTablet1.visibility = View.GONE
        ime.binding.emojiBtnTablet2.visibility = View.GONE
        ime.binding.emojiBtnTablet3.visibility = View.GONE
        ime.binding.separator4.visibility = View.GONE
        ime.binding.separator5.visibility = View.GONE
        ime.binding.separator6.visibility = View.GONE
    }
}
