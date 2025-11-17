// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import DataContract
import android.content.Context
import be.scri.helpers.data.AutoSuggestionDataManager
import be.scri.helpers.data.AutocompletionDataManager
import be.scri.helpers.data.ConjugateDataManager
import be.scri.helpers.data.ContractDataLoader
import be.scri.helpers.data.EmojiDataManager
import be.scri.helpers.data.GenderDataManager
import be.scri.helpers.data.PluralFormsManager
import be.scri.helpers.data.PrepositionDataManager
import be.scri.helpers.data.TranslationDataManager
import be.scri.helpers.data.ClipboardDataManager


/**
 * The primary entry point for all data-related operations.
 * This class acts as a facade, providing simple access to specialized managers
 * for features like emojis, gender, plurals, and conjugations.
 * @param context The application context.
 */
class DatabaseManagers(
    context: Context,
) {
    // Centralized providers for file and contract loading.
    private val fileManager = DatabaseFileManager(context)
    private val contractLoader = ContractDataLoader(context)

    // Specialized data managers, ready for use.
    val emojiManager = EmojiDataManager(fileManager)
    val clipboardManager = ClipboardDataManager(fileManager, context)
    val genderManager = GenderDataManager(fileManager)
    val pluralManager = PluralFormsManager(fileManager)
    val prepositionManager = PrepositionDataManager(fileManager)
    val translationDataManager = TranslationDataManager(context, fileManager)
    val conjugateDataManager = ConjugateDataManager(fileManager)
    val suggestionManager = AutoSuggestionDataManager(fileManager)
    val autocompletionManager = AutocompletionDataManager(fileManager)

    /**
     * A facade method to load the data contract for a given language.
     * It delegates the loading and parsing logic to the [ContractDataLoader].
     *
     * @param language The language code (e.g., "DE", "FR") for which to load the contract.
     * @return A [DataContract] object containing the language's structural metadata, or `null`
     * if not found or on error.
     */
    fun getLanguageContract(language: String): DataContract? = contractLoader.loadContract(language)
}
