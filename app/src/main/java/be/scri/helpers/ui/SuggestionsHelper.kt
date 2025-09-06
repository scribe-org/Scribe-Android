package be.scri.helpers.ui

import be.scri.services.GeneralKeyboardIME
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.core.graphics.toColorInt
import be.scri.R.color.md_grey_black_dark
import be.scri.R.color.white
import be.scri.helpers.EmojiUtils.insertEmoji
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot
import be.scri.services.GeneralKeyboardIME.Companion.SUGGESTION_SIZE
import be.scri.services.GeneralKeyboardIME.ScribeState

class SuggestionsHelper (
    private val ime: GeneralKeyboardIME,

) {
    private val handler = Handler(Looper.getMainLooper())
    private var emojiSuggestionRunnable: Runnable? = null
    private var linguisticSuggestionRunnable: Runnable? = null
    var emojiKeywords: HashMap<String, MutableList<String>>? = null
    private var emojiMaxKeywordLength: Int = 0
    private var wordSuggestionRunnable: Runnable? = null
    internal var isSingularAndPlural: Boolean = false
    var pluralWords: Set<String>? = null


    /**
     * Companion object for holding constants related to suggestion handling.
     */
    companion object {
        private const val SUGGESTION_DELAY_MS = 50L
    }

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
        nounTypeSuggestion: List<String>? = null,
        isPlural: Boolean = false,
        caseAnnotationSuggestion: MutableList<String>? = null,
        wordSuggestions: List<String>? = null,
    ): Boolean {
        if (wordSuggestions.isNullOrEmpty()) {
            return false
        }
        val suggestion1 = wordSuggestions.getOrNull(0) ?: ""
        val suggestion2 = wordSuggestions.getOrNull(1) ?: ""
        val suggestion3 = wordSuggestions.getOrNull(2) ?: ""

        val hasLinguisticSuggestion =
            nounTypeSuggestion != null ||
                isPlural ||
                caseAnnotationSuggestion != null ||
                ime.isSingularAndPlural
        val emojiCount = ime.autoSuggestEmojis?.size ?: 0
        setSuggestionButton(ime.binding.conjugateBtn, suggestion1)
        when {
            hasLinguisticSuggestion && emojiCount != 0 -> {
                ime.updateButtonVisibility(true)
            }

            hasLinguisticSuggestion && emojiCount == 0 -> {
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

}
