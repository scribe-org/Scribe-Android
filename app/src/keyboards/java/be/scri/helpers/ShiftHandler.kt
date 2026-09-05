// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import be.scri.R
import be.scri.services.GeneralKeyboardIME
import be.scri.views.KeyboardView

private const val DEFAULT_SHIFT_PERM_TOGGLE_SPEED = 500

/**
 * Encapsulates shift state machine management, caps lock double-tap timing,
 * and keyboard mode switching between letters and symbols.
 */
class ShiftHandler(
    private val ime: GeneralKeyboardIME,
) {
    var lastShiftPressTS: Long = 0L
    private val shiftPermToggleSpeed: Int = DEFAULT_SHIFT_PERM_TOGGLE_SPEED

    /**
     * Handles the logic for the Shift key. It cycles through shift states (off, on-for-one-char, caps lock)
     * on the letter keyboard, and toggles between symbol pages on the symbol keyboard.
     *
     * @param keyboardMode The current keyboard mode.
     * @param keyboardView The instance of the keyboard view.
     */
    fun handleKeyboardLetters(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
    ) {
        if (keyboardMode == ime.keyboardLetters) {
            val shiftState = keyboardView?.mKeyboard?.mShiftState ?: SHIFT_OFF
            when {
                shiftState == SHIFT_ON_PERMANENT -> keyboardView?.setShifted(SHIFT_OFF)
                System.currentTimeMillis() - lastShiftPressTS < shiftPermToggleSpeed -> keyboardView?.setShifted(SHIFT_ON_PERMANENT)
                shiftState == SHIFT_ON_ONE_CHAR -> keyboardView?.setShifted(SHIFT_OFF)
                shiftState == SHIFT_OFF -> keyboardView?.setShifted(SHIFT_ON_ONE_CHAR)
            }
            lastShiftPressTS = System.currentTimeMillis()
        } else {
            val keyboardXml =
                if (keyboardMode == ime.keyboardSymbols) {
                    ime.keyboardMode = ime.keyboardSymbolShift
                    R.xml.keys_symbols_shift
                } else {
                    ime.keyboardMode = ime.keyboardSymbols
                    ime.getPrimarySymbolKeyboardLayoutXML()
                }
            ime.keyboard = KeyboardBase(ime, keyboardXml, ime.enterKeyType, ime.getKeyboardWidth())
            keyboardView?.setKeyboard(ime.keyboard!!)
            if (keyboardXml == R.xml.keys_symbols) {
                handleModeChange(keyboardMode, keyboardView, ime)
            }
        }
    }

    /**
     * Handles switching between the letter and symbol keyboards.
     *
     * @param keyboardMode The current keyboard mode (letters or symbols).
     * @param keyboardView The instance of the keyboard view.
     * @param context The application context.
     */
    fun handleModeChange(
        keyboardMode: Int,
        keyboardView: KeyboardView?,
        context: Context = ime.applicationContext,
    ) {
        val keyboardXml =
            if (keyboardMode == ime.keyboardLetters) {
                ime.keyboardMode = ime.keyboardSymbols
                ime.getPrimarySymbolKeyboardLayoutXML()
            } else {
                ime.keyboardMode = ime.keyboardLetters
                ime.getKeyboardLayoutXML()
            }
        ime.keyboard = KeyboardBase(context, keyboardXml, ime.enterKeyType, ime.getKeyboardWidth())
        if (ime.keyboardMode == ime.keyboardLetters) {
            val wasShifted = ime.keyboard?.mShiftState == SHIFT_ON_ONE_CHAR || ime.keyboard?.mShiftState == SHIFT_ON_PERMANENT
            if (wasShifted) {
                ime.keyboard?.setShifted(ime.keyboard?.mShiftState ?: SHIFT_OFF)
            }
        }
        keyboardView?.setKeyboard(ime.keyboard!!)
        keyboardView?.invalidateAllKeys()
        if (keyboardXml == R.xml.keys_symbols) {
            ime.uiManager.setupCurrencySymbol(ime.language)
        }
    }
}
