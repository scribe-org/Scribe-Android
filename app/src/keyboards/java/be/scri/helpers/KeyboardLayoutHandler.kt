// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.text.InputType.TYPE_CLASS_DATETIME
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_MASK_CLASS
import be.scri.R
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

/**
 * Encapsulates keyboard XML layout resolution, symbol layout mapping,
 * keyboard width calculations, state-based layout XML selection, and view re-creation.
 */
class KeyboardLayoutHandler(
    private val ime: GeneralKeyboardIME,
) {
    /**
     * Resolves the XML resource ID for the active keyboard layout.
     *
     * @return The XML layout resource ID.
     */
    fun getCurrentKeyboardLayoutXML(): Int =
        when (ime.keyboardMode) {
            ime.keyboardSymbols -> getPrimarySymbolKeyboardLayoutXML()
            ime.keyboardSymbolShift -> R.xml.keys_symbols_shift
            else -> ime.getKeyboardLayoutXML()
        }

    /**
     * Resolves the primary symbol or numeric layout XML resource ID.
     *
     * @return The XML layout resource ID.
     */
    fun getPrimarySymbolKeyboardLayoutXML(): Int =
        if (ime.isNumericKeyboardActive) {
            R.xml.keys_numeric
        } else {
            R.xml.keys_symbols
        }

    /**
     * Determines which keyboard layout XML to use based on the current [ScribeState].
     *
     * @param state The current state of the Scribe keyboard.
     * @param isSubsequentArea true if this is for a secondary conjugation view.
     * @param dataSize The number of items to display, used to select an appropriate layout.
     * @return The resource ID of the keyboard layout XML.
     */
    fun getKeyboardLayoutForState(
        state: ScribeState,
        isSubsequentArea: Boolean = false,
        dataSize: Int = 0,
    ): Int =
        when (state) {
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                ime.saveConjugateModeType(ime.language)
                if (!isSubsequentArea && dataSize == 0) {
                    ime.defaultConjugateLayoutXML
                } else {
                    when (dataSize) {
                        DATA_SIZE_2 -> R.xml.conjugate_view_2x1
                        DATA_CONSTANT_3 -> R.xml.conjugate_view_1x3
                        else -> R.xml.conjugate_view_2x2
                    }
                }
            }

            else -> {
                ime.getKeyboardLayoutXML()
            }
        }

    /**
     * Calculates the width of the keyboard container.
     *
     * @return The keyboard width in pixels.
     */
    fun getKeyboardWidth(): Int =
        if (ime.isFloatingMode) {
            val density = ime.resources.displayMetrics.density
            val screenWidth = ime.resources.displayMetrics.widthPixels
            val floatWidth = (320f * density).toInt()
            Math.min(floatWidth, (screenWidth * 0.85f).toInt())
        } else {
            ime.resources.displayMetrics.widthPixels
        }

    /**
     * Re-instantiates the [KeyboardBase] and applies the updated shift state and layout.
     */
    fun recreateKeyboard() {
        if (!ime.isUiManagerInitialized) return

        val xmlId = getCurrentKeyboardLayoutXML()
        val currentShiftState = ime.keyboard?.mShiftState ?: SHIFT_OFF
        ime.keyboard = KeyboardBase(ime, xmlId, ime.enterKeyType, getKeyboardWidth())
        ime.keyboard?.setShifted(currentShiftState)
        ime.keyboardView?.setKeyboard(ime.keyboard!!)

        if (xmlId == R.xml.keys_symbols) {
            ime.uiManager.setupCurrencySymbol(ime.language)
        }
        ime.keyboardView?.invalidateAllKeys()
    }

    companion object {
        internal fun shouldUseNumericKeyboard(inputType: Int): Boolean =
            when (inputType and TYPE_MASK_CLASS) {
                TYPE_CLASS_NUMBER, TYPE_CLASS_DATETIME, TYPE_CLASS_PHONE -> true
                else -> false
            }

        internal fun getKeyboardLayoutXMLForInputType(
            inputType: Int,
            letterKeyboardLayoutXML: Int,
        ): Int =
            if (shouldUseNumericKeyboard(inputType)) {
                R.xml.keys_numeric
            } else {
                letterKeyboardLayoutXML
            }
    }
}
