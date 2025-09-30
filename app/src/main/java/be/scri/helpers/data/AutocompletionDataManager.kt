// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

// import be.scri.helpers.DatabaseFileManager

// class AutocompletionDataManager(
//     private val fileManager: DatabaseFileManager,
// ) {
class AutocompletionDataManager {
    private val trie = Trie()

    fun loadWords(language: String) {
        // val db = fileManager.getLanguageDatabase(language)
//        db?.rawQuery("SELECT word FROM dictionary", null).use { cursor ->
//            val wordIndex = cursor!!.getColumnIndex("word")
//            while (cursor.moveToNext()) {
//                val word = cursor.getString(wordIndex)?.lowercase()?.trim()
//                if (!word.isNullOrEmpty()) {
//                    trie.insert(word)
//                }
//            }
//        }

        // replace with DB query later
        val dummyWords =
            when (language) {
                "DE" ->
                    listOf( // German
                        "hallo",
                        "haus",
                        "hund",
                        "katze",
                        "kinder",
                        "essen",
                        "trinken",
                        "gehen",
                        "schlafen",
                        "lernen",
                        "auto",
                        "apfel",
                        "arbeit",
                        "baum",
                        "buch",
                        "freund",
                        "frau",
                        "mann",
                        "schule",
                        "stadt",
                    )
                else ->
                    listOf( // Default English
                        "apple",
                        "application",
                        "apply",
                        "banana",
                        "band",
                        "bandana",
                        "cat",
                        "catalog",
                        "caterpillar",
                        "dog",
                        "door",
                        "dorm",
                    )
            }
        dummyWords.forEach { trie.insert(it) }
    }

    fun getAutocompletions(
        prefix: String,
        limit: Int = 10,
    ): List<String> {
        if (prefix.length < 2) return emptyList() // only after 2 letters
        return trie.searchPrefix(prefix, limit)
    }
}
