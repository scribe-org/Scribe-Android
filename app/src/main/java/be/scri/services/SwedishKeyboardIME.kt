// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * The input method (IME) for the Swedish language keyboard.
 */

package be.scri.services

import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import be.scri.R
import be.scri.databinding.KeyboardViewCommandOptionsBinding
import be.scri.helpers.KeyboardBase
import be.scri.helpers.PreferencesHelper.getEnablePeriodAndCommaABC
import be.scri.helpers.PreferencesHelper.getIsAccentCharacterDisabled
import be.scri.helpers.PreferencesHelper.getIsPreviewEnabled
import be.scri.helpers.PreferencesHelper.getIsVibrateEnabled
import be.scri.views.KeyboardView

/**
 * The SwedishKeyboardIME class provides the input method for the Swedish language keyboard.
 */
class SwedishKeyboardIME : GeneralKeyboardIME("Swedish") {
    /**
     * Returns the XML layout resource for the keyboard based on user preferences.
     * @return The resource ID of the keyboard layout XML.
     */
    override fun getKeyboardLayoutXML(): Int =
        if (getIsAccentCharacterDisabled(applicationContext, language) &&
            !getEnablePeriodAndCommaABC(applicationContext, language)
        ) {
            R.xml.keys_letter_swedish_without_accent_characters_and_without_period_and_comma
        } else if (!getIsAccentCharacterDisabled(applicationContext, language) &&
            getEnablePeriodAndCommaABC(applicationContext, language)
        ) {
            R.xml.keys_letters_swedish
        } else if (getIsAccentCharacterDisabled(applicationContext, language) &&
            getEnablePeriodAndCommaABC(applicationContext, language)
        ) {
            R.xml.keys_letter_swedish_without_accent_characters
        } else {
            R.xml.keys_letter_swedish_without_period_and_comma
        }

    override lateinit var binding: KeyboardViewCommandOptionsBinding
    override var keyboardView: KeyboardView? = null
    override var keyboard: KeyboardBase? = null
    override var enterKeyType = IME_ACTION_NONE
    override val keyboardLetters = 0
    override val keyboardSymbols = 1
    override val keyboardSymbolShift = 2
    override var lastShiftPressTS = 0L
    override var keyboardMode = keyboardLetters
    override var inputTypeClass = InputType.TYPE_CLASS_TEXT
    override var switchToLetters = false
    override var hasTextBeforeCursor = false

    /**
     * Creates and returns the input view for the keyboard.
     * @return The root view of the keyboard layout.
     */
    override fun onCreateInputView(): View {
        binding = KeyboardViewCommandOptionsBinding.inflate(layoutInflater)
        val keyboardHolder = binding.root
        Log.i("MY-TAG", "From Swedish Keyboard IME")
        keyboardView = binding.keyboardView
        keyboardView!!.setKeyboard(keyboard!!)
        setupCommandBarTheme(binding)
        keyboardView!!.setPreview = getIsPreviewEnabled(applicationContext, language)
        keyboardView!!.setVibrate = getIsVibrateEnabled(applicationContext, language)
        keyboardView!!.setKeyboardHolder()
        keyboardView!!.mOnKeyboardActionListener = this
        initializeEmojiButtons()
        updateUI()
        return keyboardHolder
    }

    /**
     * Handles key press events on the keyboard.
     * @param code The key code of the pressed key.
     */
    override fun onKey(code: Int) {
        val inputConnection = currentInputConnection
        if (keyboard == null || inputConnection == null) {
            return
        }
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            lastShiftPressTS = 0
        }

        when (code) {
            KeyboardBase.KEYCODE_DELETE -> {
                handleKeycodeDelete()
                keyboardView!!.invalidateAllKeys()
                disableAutoSuggest()
            }

            KeyboardBase.KEYCODE_SHIFT -> {
                super.handleKeyboardLetters(keyboardMode, keyboardView)
                keyboardView!!.invalidateAllKeys()
                disableAutoSuggest()
            }

            KeyboardBase.KEYCODE_ENTER -> {
                disableAutoSuggest()
                handleKeycodeEnter()
                updateAutoSuggestText(isPlural = checkIfPluralWord, nounTypeSuggestion = nounTypeSuggestion)
            }

            KeyboardBase.KEYCODE_MODE_CHANGE -> {
                handleModeChange(keyboardMode, keyboardView, this)
                disableAutoSuggest()
            }

            KeyboardBase.KEYCODE_SPACE -> {
                handleKeycodeSpace()
            }

            else -> {
                if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
                    handleElseCondition(code, keyboardMode, binding = null)
                    disableAutoSuggest()
                } else {
                    handleElseCondition(code, keyboardMode, keyboardBinding, commandBarState = true)
                    disableAutoSuggest()
                }
            }
        }

        lastWord = getLastWordBeforeCursor()
        Log.d("Debug", "$lastWord")
        autoSuggestEmojis = findEmojisForLastWord(emojiKeywords, lastWord)
        nounTypeSuggestion = findGenderForLastWord(nounKeywords, lastWord)
        checkIfPluralWord = findWhetherWordIsPlural(pluralWords, lastWord)
        Log.d("Debug", "$autoSuggestEmojis")
        Log.d("MY-TAG", "$nounTypeSuggestion")
        updateButtonText(emojiAutoSuggestionEnabled, autoSuggestEmojis)
        if (code != KeyboardBase.KEYCODE_SHIFT) {
            super.updateShiftKeyState()
        }
    }

    /**
     * Handles the delete key press event.
     */
    fun handleKeycodeDelete() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleDelete(false, keyboardBinding)
        } else {
            handleDelete(true, keyboardBinding)
        }
    }

    /**
     * Handles the enter key press event.
     */
    fun handleKeycodeEnter() {
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleKeycodeEnter(keyboardBinding, false)
        } else {
            handleKeycodeEnter(keyboardBinding, true)
            currentState = ScribeState.IDLE
            switchToCommandToolBar()
            updateUI()
        }
    }

    /**
     * Handles the space key press event.
     */
    fun handleKeycodeSpace() {
        val code = KeyboardBase.KEYCODE_SPACE
        if (currentState == ScribeState.IDLE || currentState == ScribeState.SELECT_COMMAND) {
            handleElseCondition(code, keyboardMode, binding = null)
            updateAutoSuggestText(isPlural = checkIfPluralWord, nounTypeSuggestion = nounTypeSuggestion)
        } else {
            handleElseCondition(code, keyboardMode, keyboardBinding, commandBarState = true)
            disableAutoSuggest()
        }
    }

    /**
     * Initializes the keyboard and sets up the input view.
     */
    override fun onCreate() {
        super.onCreate()
        keyboard = KeyboardBase(this, getKeyboardLayoutXML(), enterKeyType)
        onCreateInputView()
        setupCommandBarTheme(binding)
    }
}
