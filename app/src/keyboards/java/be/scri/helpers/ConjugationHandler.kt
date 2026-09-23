// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context.MODE_PRIVATE
import android.view.View
import androidx.core.content.edit
import be.scri.R
import be.scri.models.ScribeState

private const val DATA_SIZE_2 = 2
private const val DATA_SIZE_3 = 3

/**
 * Encapsulates verb conjugation state management, conjugation table layout logic,
 * and capitalization formatting for GeneralKeyboardIME.
 */
class ConjugationHandler(
    private val ime: KeyboardIMEContext,
) {
    var subsequentAreaRequired: Boolean = false
        private set
    var subsequentData: MutableList<List<String>> = mutableListOf()

    /**
     * Saves the type of conjugation layout being used (e.g., "2x2", "none") to shared preferences.
     *
     * For the **primary conjugation view** (isSubsequent = false):
     * - If [language] is `"none"` (idle/reset sentinel), saves `"none"` directly.
     * - Otherwise uses [KeyboardIMEContext.defaultConjugateModeType], so each IME subclass
     *   controls its own mode without a hardcoded language list here.
     *
     * For a **sub-view** (isSubsequent = true), saves [subViewMode] (e.g. "2x1" or "1x3")
     * so that [be.scri.helpers.KeyboardBase] picks the correct row height.
     *
     * @param language The current keyboard language, or "none" to reset to the idle mode.
     * @param isSubsequent true if saving for a sub-view, false for the standard conjugation view.
     * @param subViewMode The layout mode string for the sub-view. Only used when [isSubsequent] is true.
     */
    fun saveConjugateModeType(
        language: String,
        isSubsequent: Boolean = false,
        subViewMode: String = "none",
    ) {
        val mode =
            when {
                isSubsequent -> subViewMode
                language == "none" -> "none"
                else -> ime.defaultConjugateModeType
            }
        val sharedPref = ime.imeContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        sharedPref.edit { putString("conjugate_mode_type", mode) }
    }

    /**
     * Handles a key press on one of the special conjugation keys.
     * It either commits the text directly or prepares for a subsequent selection view.
     *
     * @param code The key code of the pressed key.
     * @param isSubsequentRequired true if a sub-view is needed for more options.
     *
     * @return The label of the key that was pressed.
     */
    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean,
    ): String? {
        val keyLabel = ime.keyboardView?.getKeyLabel(code)
        if (!isSubsequentRequired) {
            if (!keyLabel.isNullOrEmpty()) {
                ime.getInputConnection()?.commitText("$keyLabel ", 1)
                ime.suggestionHandler.processLinguisticSuggestions(keyLabel)
            }
        }
        return keyLabel
    }

    /**
     * Sets up a secondary "sub-view" for conjugation when a single key has multiple options.
     *
     * @param data The full dataset of subsequent options.
     * @param word The specific word selected from the primary view, used to filter the data.
     */
    fun setupConjugateSubView(
        data: List<List<String>>,
        word: String?,
    ) {
        val uniqueData = data.distinct()
        val filteredData = uniqueData.filter { sublist -> sublist.contains(word) }
        val flattenList = filteredData.flatten()
        val keyboardXmlId = getKeyboardLayoutForState(ime.currentState, dataSize = flattenList.size)
        val subViewMode =
            when (flattenList.size) {
                DATA_SIZE_2 -> "2x1"
                DATA_SIZE_3 -> "1x3"
                else -> return
            }
        saveConjugateModeType(language = ime.language, isSubsequent = true, subViewMode = subViewMode)
        ime.uiManager.initializeKeyboard(keyboardXmlId)
        when (flattenList.size) {
            DATA_SIZE_2 -> {
                ime.keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_2X1_TOP)
                ime.keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_2X1_BOTTOM)
                subsequentAreaRequired = false
            }

            DATA_SIZE_3 -> {
                ime.keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_1X3_LEFT)
                ime.keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_1X3_CENTER)
                ime.keyboardView?.setKeyLabel(flattenList[DATA_SIZE_2], "HI", KeyboardBase.CODE_1X3_RIGHT)
                subsequentAreaRequired = false
            }
        }
        ime.uiManager.binding.ivInfo.visibility = View.GONE
    }

    /**
     * Determines which keyboard layout XML to use for a conjugation sub-view based on the number
     * of items in the sub-view. Only called from [setupConjugateSubView] for sizes 2 and 3;
     * other sizes trigger an early return before this is used.
     *
     * @param state The current state of the Scribe keyboard.
     * @param dataSize The number of items to display, used to select an appropriate layout.
     *
     * @return The resource ID of the keyboard layout XML.
     */
    private fun getKeyboardLayoutForState(
        state: ScribeState,
        dataSize: Int,
    ): Int =
        when (state) {
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                when (dataSize) {
                    DATA_SIZE_2 -> R.xml.conjugate_view_2x1
                    DATA_SIZE_3 -> R.xml.conjugate_view_1x3
                    else -> ime.defaultConjugateLayoutXML
                }
            }

            else -> {
                ime.getKeyboardLayoutXML()
            }
        }

    companion object {
        /**
         * Applies capitalization to all conjugated forms in the output map.
         * Supports both standard capitalization (first letter) and all capital letters formatting.
         *
         * @param conjugations The original map of conjugations from the database.
         * @param isAllCaps If true, applies all capital letters; if false, capitalizes only first letter.
         *
         * @return A new map with properly formatted conjugations.
         */
        fun applyCapitalizationToConjugations(
            conjugations: MutableMap<String, MutableMap<String, Collection<String>>>,
            isAllCaps: Boolean = false,
        ): MutableMap<String, MutableMap<String, Collection<String>>> {
            val formattedOutput: MutableMap<String, MutableMap<String, Collection<String>>> = mutableMapOf()
            conjugations.forEach { (tenseKey, conjugationMap) ->
                val formattedConjugations: MutableMap<String, Collection<String>> = mutableMapOf()
                conjugationMap.forEach { (categoryKey, forms) ->
                    val formattedForms =
                        forms.map { form ->
                            when {
                                form.isEmpty() -> form
                                isAllCaps -> form.uppercase()
                                else -> form.replaceFirstChar { it.uppercase() }
                            }
                        }
                    formattedConjugations[categoryKey] = formattedForms
                }
                formattedOutput[tenseKey] = formattedConjugations
            }
            return formattedOutput
        }
    }
}
