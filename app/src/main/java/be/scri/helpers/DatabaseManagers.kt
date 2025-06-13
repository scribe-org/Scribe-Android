// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import DataContract
import android.content.Context
import be.scri.helpers.data.ConjugateDataManager
import be.scri.helpers.data.ContractDataLoader
import be.scri.helpers.data.EmojiDataManager
import be.scri.helpers.data.GenderDataManager
import be.scri.helpers.data.PluralFormsManager
import be.scri.helpers.data.PrepositionDataManager
import be.scri.helpers.data.TranslationDataManager

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
    val genderManager = GenderDataManager(fileManager)
    val pluralManager = PluralFormsManager(fileManager)
    val prepositionManager = PrepositionDataManager(fileManager)
    val translationDataManager = TranslationDataManager(context, fileManager)
    val conjugateDataManager = ConjugateDataManager(fileManager)

    /**
     * Retrieves the data contract for a given language.
     * @param language The language code (e.g., "DE", "FR").
     * @return A [DataContract] object, or null if not found.
     */
    fun getLanguageContract(language: String): DataContract? = contractLoader.loadContract(language)
}
