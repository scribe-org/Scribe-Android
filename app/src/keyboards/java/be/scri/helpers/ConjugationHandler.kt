// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context.MODE_PRIVATE
import android.view.View
import androidx.core.content.edit
import be.scri.R
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

/**
 * Encapsulates verb conjugation state management, conjugation table layout logic,
 * and capitalization formatting for GeneralKeyboardIME.
 */
class ConjugationHandler(
    private val ime: GeneralKeyboardIME,
) {
    var subsequentAreaRequired: Boolean = false
    var subsequentData: MutableList<List<String>> = mutableListOf()

    var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>?
        get() = ime.dataHandler.conjugateOutput
        set(value) {
            ime.dataHandler.conjugateOutput = value
        }

    var conjugateLabels: Set<String>
        get() = ime.dataHandler.conjugateLabels
        set(value) {
            ime.dataHandler.conjugateLabels = value
        }

    /**
     * Saves the type of conjugation layout being used (e.g., "2x2", "3x2") to shared preferences.
     *
     * @param language The current keyboard language.
     * @param isSubsequent true if saving for a sub-view, false for standard layout.
     */
    fun saveConjugateModeType(
        language: String,
        isSubsequent: Boolean = false,
    ) {
        val mode =
            if (isSubsequent) {
                "2x1"
            } else {
                when (getLanguageAlias(language).lowercase()) {
                    "es", "it" -> "3x2"
                    else -> "2x2"
                }
            }
        val sharedPref = ime.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        sharedPref.edit { putString("conjugate_mode_type", mode) }
    }

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

    /**
     * Retrieves and validates the stored index for the current conjugation view.
     * Ensures the index is within the bounds of available conjugation types.
     *
     * @return A valid, zero-based index for the conjugation type.
     */
    fun getValidatedConjugateIndex(): Int {
        val prefs = ime.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        var index = prefs.getInt("conjugate_index", 0)
        val maxIndex = conjugateOutput?.keys?.count()?.minus(1) ?: -1
        index = if (maxIndex >= 0) index.coerceIn(0, maxIndex) else 0
        prefs.edit { putInt("conjugate_index", index) }
        return index
    }

    /**
     * Returns whether the current conjugation state requires a subsequent selection view.
     */
    fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    fun returnSubsequentData(): List<List<String>> = subsequentData

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
                ime.currentInputConnection?.commitText("$keyLabel ", 1)
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
        saveConjugateModeType(language = ime.language, isSubsequent = true)
        val prefs = ime.applicationContext.getSharedPreferences("keyboard_preferences", MODE_PRIVATE)
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        val keyboardXmlId = getKeyboardLayoutForState(ime.currentState, isSubsequentArea = true, dataSize = flattenList.size)
        ime.uiManager.initializeKeyboard(keyboardXmlId)
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        when (flattenList.size) {
            DATA_SIZE_2 -> {
                ime.keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_2X1_TOP)
                ime.keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_2X1_BOTTOM)
                subsequentAreaRequired = false
            }

            DATA_CONSTANT_3 -> {
                ime.keyboardView?.setKeyLabel(flattenList[0], "HI", KeyboardBase.CODE_1X3_RIGHT)
                ime.keyboardView?.setKeyLabel(flattenList[1], "HI", KeyboardBase.CODE_1X3_CENTER)
                ime.keyboardView?.setKeyLabel(flattenList[DATA_SIZE_2], "HI", KeyboardBase.CODE_1X3_RIGHT)
                subsequentAreaRequired = false
            }
        }
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        ime.uiManager.binding.ivInfo.visibility = View.GONE
    }

    /**
     * Determines which keyboard layout XML to use based on the current [ScribeState].
     *
     * @param state The current state of the Scribe keyboard.
     * @param isSubsequentArea true if this is for a secondary conjugation view.
     * @param dataSize The number of items to display, used to select an appropriate layout.
     *
     * @return The resource ID of the keyboard layout XML.
     */
    fun getKeyboardLayoutForState(
        state: ScribeState,
        isSubsequentArea: Boolean = false,
        dataSize: Int = 0,
    ): Int =
        when (state) {
            ScribeState.SELECT_VERB_CONJUNCTION -> {
                saveConjugateModeType(ime.language)
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
}
