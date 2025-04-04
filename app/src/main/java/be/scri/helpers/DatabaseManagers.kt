// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import ContractDataLoader
import EmojiDataManager
import GenderDataManager
import PluralFormsManager
import android.content.Context
import be.scri.helpers.keyboardDBHelper.PrepositionDataManager

/**
 * A helper class that manages various database-related operations
 * and data managers for the Scribe keyboard.
 * This class provides access to all the necessary managers that interact
 * with the database for different features such as contracts, emojis, gender
 * data, plural forms, and prepositions.
 *
 * @param context The context used to access the app's resources and database.
 */
class DatabaseManagers(
    context: Context,
) {
    val fileManager = DatabaseFileManager(context)
    val contractLoader = ContractDataLoader(context)
    val emojiManager = EmojiDataManager(context)
    val genderManager = GenderDataManager(context)
    val pluralManager = PluralFormsManager(context)
    val prepositionManager = PrepositionDataManager(context)
}
