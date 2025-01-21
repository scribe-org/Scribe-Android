package be.scri.helpers

import ContractDataLoader
import EmojiDataManager
import GenderDataManager
import PluralFormsManager
import android.content.Context

class DatabaseManagers(
    context: Context,
) {
    val fileManager = DatabaseFileManager(context)
    val contractLoader = ContractDataLoader(context)
    val emojiManager = EmojiDataManager(context)
    val genderManager = GenderDataManager(context)
    val pluralManager = PluralFormsManager(context)
}
