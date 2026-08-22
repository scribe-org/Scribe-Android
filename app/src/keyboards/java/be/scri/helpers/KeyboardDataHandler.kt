// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import DataContract
import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import be.scri.helpers.LanguageMappingConstants.getLanguageAlias
import be.scri.helpers.data.AutocompletionDataManager
import be.scri.helpers.english.ENInterfaceVariables.ALREADY_PLURAL_MSG

/**
 * Manages database initializations, data contract loading, dictionary queries,
 * translations, plurals, and autocompletion data for the keyboard system.
 */
class KeyboardDataHandler {
    lateinit var dbManagers: DatabaseManagers
        private set

    lateinit var autocompletionManager: AutocompletionDataManager
        private set

    var dataContract: DataContract? = null
        internal set

    var emojiKeywords: HashMap<String, MutableList<String>>? = null
        internal set

    var emojiMaxKeywordLength: Int = 0
        internal set

    var pluralWords: Set<String>? = null
        internal set

    lateinit var nounKeywords: HashMap<String, List<String>>
        internal set

    lateinit var suggestionWords: HashMap<String, List<String>>
        internal set

    lateinit var caseAnnotation: HashMap<String, MutableList<String>>
        internal set

    var conjugateOutput: MutableMap<String, MutableMap<String, Collection<String>>>? = null
        internal set

    var conjugateLabels: Set<String> = emptySet()
        internal set

    val isInitialized: Boolean
        get() = this::dbManagers.isInitialized

    /**
     * Initializes the database managers using the given [context].
     */
    fun initialize(context: Context) {
        dbManagers = DatabaseManagers(context)
        autocompletionManager = dbManagers.autocompletionManager
    }

    /**
     * Loads dictionary, emoji, plural, gender, preposition, and conjugate data for [language].
     */
    fun loadLanguageData(language: String) {
        val languageAlias = getLanguageAlias(language)
        dataContract = dbManagers.getLanguageContract(languageAlias)
        emojiKeywords = dbManagers.emojiManager.getEmojiKeywords(languageAlias)
        emojiMaxKeywordLength = dbManagers.emojiManager.maxKeywordLength
        pluralWords =
            dbManagers.pluralManager
                .getAllPluralForms(languageAlias, dataContract)
                ?.map { it.lowercase() }
                ?.toSet()
        nounKeywords = dbManagers.genderManager.findGenderOfWord(languageAlias, dataContract)
        suggestionWords = dbManagers.suggestionManager.getSuggestions(languageAlias)
        val numbersColumns =
            dataContract?.numbers?.let { map ->
                (map.keys + map.values).distinct()
            } ?: emptyList()
        autocompletionManager.loadWords(languageAlias, numbersColumns)
        caseAnnotation = dbManagers.prepositionManager.getCaseAnnotations(languageAlias)

        val tempConjugateOutput = dbManagers.conjugateDataManager.getTheConjugateLabels(languageAlias, dataContract, "describe")
        conjugateOutput = if (tempConjugateOutput?.isEmpty() == true) null else tempConjugateOutput
        conjugateLabels = dbManagers.conjugateDataManager.extractConjugateHeadings(dataContract, "coacha")
    }

    /**
     * Queries autocompletion suggestions for [prefix] up to [limit].
     */
    fun getAutocompletions(
        prefix: String,
        limit: Int = 3,
    ): List<String> =
        try {
            autocompletionManager.getAutocompletions(prefix, limit)
        } catch (e: SQLiteException) {
            Log.e("KeyboardDataHandler", "Database error in autocompletion", e)
            emptyList()
        } catch (e: IllegalStateException) {
            Log.e("KeyboardDataHandler", "Illegal state in autocompletion", e)
            emptyList()
        }

    /**
     * Retrieves the plural form of [word] for [language].
     */
    fun getPluralRepresentation(
        language: String,
        word: String?,
    ): String? {
        if (word.isNullOrEmpty()) return null
        val langAlias = getLanguageAlias(language)
        val lowercaseWord = word.lowercase()
        if (pluralWords?.contains(lowercaseWord) == true) return ALREADY_PLURAL_MSG
        return dbManagers.pluralManager
            .getPluralRepresentation(langAlias, dataContract, word)
            .values
            .firstOrNull()
    }

    /**
     * Retrieves the translation for [commandBarInput] in [language].
     */
    fun getTranslation(
        language: String,
        commandBarInput: String,
    ): String {
        val sourceDest = dbManagers.translationDataManager.getSourceAndDestinationLanguage(language)
        return dbManagers.translationDataManager.getTranslationDataForAWord(sourceDest, commandBarInput)
    }

    /**
     * Queries conjugate labels and headings for [searchInput] in [language].
     */
    fun queryConjugateData(
        language: String,
        searchInput: String,
    ): Pair<MutableMap<String, MutableMap<String, Collection<String>>>?, Set<String>> {
        val languageAlias = getLanguageAlias(language)
        val tempOutput = dbManagers.conjugateDataManager.getTheConjugateLabels(languageAlias, dataContract, searchInput)
        val output =
            if (tempOutput?.isEmpty() == true || tempOutput?.values?.all { it.isEmpty() } == true) {
                null
            } else {
                tempOutput
            }
        val labels = dbManagers.conjugateDataManager.extractConjugateHeadings(dataContract, searchInput)
        return Pair(output, labels)
    }
}
