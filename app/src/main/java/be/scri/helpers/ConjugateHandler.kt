// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.content.edit
import be.scri.R
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

private const val DATA_SIZE_2 = 2
private const val DATA_CONSTANT_3 = 3

/**
 * Handles all conjugation-related functionality for the Scribe keyboard.
 * This includes managing conjugation data, keyboard layouts, and UI updates for verb conjugation.
 */
@Suppress("TooManyFunctions", "LargeClass")
class ConjugateHandler(
    private val ime: GeneralKeyboardIME,
) {
    private var subsequentAreaRequired: Boolean = false
    private var subsequentData: MutableList<List<String>> = mutableListOf()

    private lateinit var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>
    private lateinit var conjugateLabels: Set<String>

    /**
     * Returns whether the current conjugation state requires a subsequent selection view.
     * This is used, for example, when a conjugation form has multiple options (e.g., "am/is/are" in English).
     * @return `true` if a subsequent selection screen is needed, `false` otherwise.
     */
    fun returnIsSubsequentRequired(): Boolean = subsequentAreaRequired

    /**
     * Returns the subsequent data for conjugation selection.
     * @return List of conjugation options for subsequent selection.
     */
    fun returnSubsequentData(): List<List<String>> = subsequentData

    /**
     * Determines which keyboard layout XML to use for conjugation based on the current state.
     * @param language The current keyboard language.
     * @param isSubsequentArea `true` if this is for a secondary conjugation view.
     * @param dataSize The number of items to display, used to select an appropriate layout.
     * @return The resource ID of the keyboard layout XML.
     */
    fun getConjugateKeyboardLayout(
        language: String,
        isSubsequentArea: Boolean = false,
        dataSize: Int = 0,
    ): Int {
        saveConjugateModeType(language)
        return if (!isSubsequentArea && dataSize == 0) {
            when (language) {
                "English", "Russian", "Swedish" -> R.xml.conjugate_view_2x2
                else -> R.xml.conjugate_view_3x2
            }
        } else {
            when (dataSize) {
                DATA_SIZE_2 -> R.xml.conjugate_view_2x1
                DATA_CONSTANT_3 -> R.xml.conjugate_view_1x3
                else -> R.xml.conjugate_view_2x2
            }
        }
    }

    /**
     * A wrapper to set up the conjugation key labels for the current language and index.
     * @param conjugateIndex The index of the conjugation tense/mood to display.
     * @param isSubsequentArea `true` if setting up a secondary view.
     */
    fun setupConjugateKeysByLanguage(
        conjugateIndex: Int,
        isSubsequentArea: Boolean = false,
    ) {
        setUpConjugateKeys(
            startIndex = conjugateIndex,
            isSubsequentArea = isSubsequentArea,
        )
    }

    /**
     * Sets the labels for the special conjugation keys based on the selected tense/mood.
     * @param startIndex The index of the conjugation tense/mood from the loaded data.
     * @param isSubsequentArea `true` if this is for a secondary conjugation view.
     */
    internal fun setUpConjugateKeys(
        startIndex: Int,
        isSubsequentArea: Boolean,
    ) {
        if (!this::conjugateOutput.isInitialized || !this::conjugateLabels.isInitialized) {
            return
        }

        val title = conjugateOutput.keys.elementAtOrNull(startIndex)
        val languageOutput = title?.let { conjugateOutput[it] }

        if (conjugateLabels.isEmpty() || title == null || languageOutput == null) {
            return
        }

        Log.i("HELLO", "The output from the languageOutput is $languageOutput")
        if (ime.language != "English") {
            setUpNonEnglishConjugateKeys(languageOutput, conjugateLabels.toList(), title)
        } else {
            setUpEnglishConjugateKeys(languageOutput, isSubsequentArea)
        }

        if (isSubsequentArea) {
            ime.keyboardView?.setKeyLabel("HI", "HI", KeyboardBase.CODE_FPS)
        }
    }

    /**
     * Sets up conjugation key labels for non-English languages, which typically follow a 3x2 grid layout.
     * @param languageOutput The map of conjugation forms for the selected tense.
     * @param conjugateLabel The list of labels for each person/number (e.g., "1ps", "2ps").
     * @param title The title of the current tense/mood.
     */
    private fun setUpNonEnglishConjugateKeys(
        languageOutput: Map<String, Collection<String>>,
        conjugateLabel: List<String>,
        title: String,
    ) {
        val keyCodes =
            when (ime.language) {
                "Swedish" -> {
                    listOf(
                        KeyboardBase.CODE_TR,
                        KeyboardBase.CODE_TL,
                        KeyboardBase.CODE_BR,
                        KeyboardBase.CODE_BL,
                    )
                }
                else -> {
                    listOf(
                        KeyboardBase.CODE_FPS,
                        KeyboardBase.CODE_FPP,
                        KeyboardBase.CODE_SPS,
                        KeyboardBase.CODE_SPP,
                        KeyboardBase.CODE_TPS,
                        KeyboardBase.CODE_TPP,
                    )
                }
            }

        keyCodes.forEachIndexed { index, code ->
            val value = languageOutput[title]?.elementAtOrNull(index) ?: ""
            ime.keyboardView?.setKeyLabel(value, conjugateLabel.getOrNull(index) ?: "", code)
        }
    }

    /**
     * Sets up conjugation key labels for English, which has a more complex structure,
     * potentially requiring a subsequent selection view.
     * @param languageOutput The map of conjugation forms for the selected tense.
     * @param isSubsequentArea `true` if this is for a secondary view.
     */
    private fun setUpEnglishConjugateKeys(
        languageOutput: Map<String, Collection<String>>,
        isSubsequentArea: Boolean,
    ) {
        val keys = languageOutput.keys.toList()
        val sharedPreferences = ime.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)

        val keyMapping =
            listOf(
                Triple(0, KeyboardBase.CODE_TL, "CODE_TL"),
                Triple(1, KeyboardBase.CODE_TR, "CODE_TR"),
                Triple(DATA_SIZE_2, KeyboardBase.CODE_BL, "CODE_BL"),
                Triple(DATA_CONSTANT_3, KeyboardBase.CODE_BR, "CODE_BR"),
            )

        if (!isSubsequentArea) {
            keyMapping.forEach { (_, code, _) -> ime.keyboardView?.setKeyLabel("HI", "HI", code) }
        }

        subsequentAreaRequired = false
        keyMapping.forEach { (index, code, prefKey) ->
            val outputKey = keys.getOrNull(index)
            val output = outputKey?.let { languageOutput[it] }

            if (output != null) {
                if (output.size > 1) {
                    subsequentAreaRequired = true
                    subsequentData.add(output.toList())
                    sharedPreferences.edit { putString("1", prefKey) }
                } else {
                    sharedPreferences.edit { putString("0", prefKey) }
                }
                ime.keyboardView?.setKeyLabel(output.firstOrNull().toString(), "HI", code)
            }
        }
    }

    /**
     * Sets up a secondary "sub-view" for conjugation when a single key has multiple options.
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
        saveConjugateModeType(language = ime.language, true)
        val prefs = ime.applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        prefs.edit(commit = true) { putString("conjugate_mode_type", "2x1") }
        val keyboardXmlId = getConjugateKeyboardLayout(ime.language, true, flattenList.size)
        ime.initializeKeyboard(keyboardXmlId)
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
        ime.binding.ivInfo.visibility = View.GONE
    }

    /**
     * Saves the type of conjugation layout being used (e.g., "2x2", "3x2") to shared preferences.
     * @param language The current keyboard language.
     * @param isSubsequentArea `true` if this is for a secondary view.
     */
    fun saveConjugateModeType(
        language: String,
        isSubsequentArea: Boolean = false,
    ) {
        val sharedPref = ime.applicationContext.getSharedPreferences("keyboard_preferences", Context.MODE_PRIVATE)
        val mode =
            if (!isSubsequentArea) {
                when (language) {
                    "English", "Russian", "Swedish" -> "2x2"
                    "German", "French", "Italian", "Portuguese", "Spanish" -> "3x2"
                    else -> "none"
                }
            } else {
                "none"
            }
        sharedPref.edit { putString("conjugate_mode_type", mode) }
    }

    /**
     * Handles a key press on one of the special conjugation keys.
     * It either commits the text directly or prepares for a subsequent selection view.
     * @param code The key code of the pressed key.
     * @param isSubsequentRequired `true` if a sub-view is needed for more options.
     * @return The label of the key that was pressed.
     */
    fun handleConjugateKeys(
        code: Int,
        isSubsequentRequired: Boolean,
    ): String? {
        val keyLabel = ime.keyboardView?.getKeyLabel(code)
        if (!isSubsequentRequired) {
            ime.currentInputConnection?.commitText(keyLabel, 1)
            ime.suggestionHandler.processLinguisticSuggestions(keyLabel)
        }
        return keyLabel
    }

    /**
     * Handles the Enter key press when in the `CONJUGATE` state. It fetches the
     * conjugation data for the entered verb and transitions to the selection view.
     * @param rawInput The verb entered in the command bar.
     * @return The new ScribeState after processing the conjugate command.
     */
    fun handleConjugateState(rawInput: String) {
        val languageAlias = getLanguageAlias(ime.language)
        conjugateOutput =
            ime.dbManagers.conjugateDataManager.getTheConjugateLabels(
                languageAlias,
                ime.dataContract,
                rawInput.lowercase(),
            )

        conjugateLabels =
            ime.dbManagers.conjugateDataManager.extractConjugateHeadings(
                ime.dataContract,
                rawInput.lowercase(),
            )

        ime.currentState =
            if (
                conjugateOutput.isEmpty() ||
                conjugateOutput.values.all { it.isEmpty() }
            ) {
                ScribeState.INVALID
            } else {
                saveConjugateModeType(ime.language)
                ScribeState.SELECT_VERB_CONJUNCTION
            }

        ime.updateUI()
    }
}
