// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * A helper class that manages various database-related operations
 * and data managers for the Scribe keyboard.
*/

package be.scri.helpers

import ContractDataLoader
import EmojiDataManager
import GenderDataManager
import PluralFormsManager
import android.content.Context
import be.scri.helpers.keyboardDBHelper.PrepositionDataManager

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
