// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.util.Log
import be.scri.inputmethod.latin.BinaryDictionary
import be.scri.inputmethod.keyboard.ProximityInfo
import be.scri.latin.NgramContext
import be.scri.latin.common.ComposedData
import be.scri.latin.dictionary.ReadOnlyBinaryDictionary
import be.scri.latin.settings.SettingsValuesForSuggestion
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

/**
 * Handles offloading autocompletion and word suggestions to the native C++ HeliBoard
 * dictionary engine compiled via Android NDK (libjni_latinime.so).
 */
class NativeSuggestionEngine(private val context: Context) {

    companion object {
        private const val TAG = "NativeSuggestionEngine"
        private const val DICT_DIR = "dicts"
    }

    private val loadedDicts = HashMap<String, ReadOnlyBinaryDictionary>()
    private val dummyProximityInfo = ProximityInfo()

    /**
     * Map a language string to its corresponding main dictionary asset name and Locale.
     */
    private fun getDictInfo(language: String): Pair<String, Locale>? {
        return when (language.lowercase(Locale.ROOT)) {
            "english" -> Pair("main_en-US.dict", Locale.US)
            "german" -> Pair("main_de.dict", Locale.GERMAN)
            "spanish" -> Pair("main_es.dict", Locale("es"))
            "french" -> Pair("main_fr.dict", Locale.FRENCH)
            "italian" -> Pair("main_it.dict", Locale.ITALIAN)
            "portuguese" -> Pair("main_pt-BR.dict", Locale("pt"))
            "russian" -> Pair("main_ru.dict", Locale("ru"))
            "swedish" -> Pair("main_sv.dict", Locale("sv"))
            else -> null
        }
    }

    /**
     * Extracts a dictionary file from the assets to internal storage if not already extracted.
     */
    private fun getOrExtractDictFile(assetName: String): File? {
        val dictsFolder = File(context.filesDir, DICT_DIR)
        if (!dictsFolder.exists() && !dictsFolder.mkdirs()) {
            Log.e(TAG, "Failed to create dicts directory")
            return null
        }

        val targetFile = File(dictsFolder, assetName)
        if (targetFile.exists() && targetFile.length() > 0) {
            return targetFile
        }

        try {
            context.assets.open("dicts/$assetName").use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.i(TAG, "Successfully extracted native dictionary: $assetName")
            return targetFile
        } catch (e: IOException) {
            Log.e(TAG, "Error extracting native dictionary $assetName from assets", e)
            return null
        }
    }

    /**
     * Retrieves or loads the BinaryDictionary for the given language.
     */
    @Synchronized
    fun getDictionary(language: String): ReadOnlyBinaryDictionary? {
        val cacheKey = language.lowercase(Locale.ROOT)
        loadedDicts[cacheKey]?.let { return it }

        val (assetName, locale) = getDictInfo(language) ?: return null
        val dictFile = getOrExtractDictFile(assetName) ?: return null

        return try {
            val dict = ReadOnlyBinaryDictionary(
                dictFile.absolutePath,
                0L,
                dictFile.length(),
                false, // useFullEditDistance
                locale,
                "main"
            )
            if (dict.isValidDictionary) {
                loadedDicts[cacheKey] = dict
                Log.i(TAG, "Successfully loaded native dictionary for $language")
                dict
            } else {
                Log.e(TAG, "Loaded dictionary for $language is invalid")
                dict.close()
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ReadOnlyBinaryDictionary for $language", e)
            null
        }
    }

    /**
     * Queries the native dictionary engine for autocomplete suggestions given a typed prefix.
     */
    fun getAutocompletions(
        language: String,
        prefix: String,
        limit: Int = 3
    ): List<String> {
        val dict = getDictionary(language) ?: return emptyList()
        if (prefix.isBlank()) return emptyList()

        return try {
            val composedData = ComposedData.createForWord(prefix)
            val suggestions = dict.getSuggestions(
                composedData,
                NgramContext.EMPTY_PREV_WORDS_INFO,
                dummyProximityInfo.nativeProximityInfo, // proximityInfoHandle
                SettingsValuesForSuggestion(false, false),
                1, // sessionId
                1.0f, // weightForLocale
                null // inOutWeightOfLangModelVsSpatialModel
            )

            suggestions?.map { it.mWord }
                ?.filter { it.isNotBlank() && it.lowercase(Locale.ROOT) != prefix.lowercase(Locale.ROOT) }
                ?.take(limit)
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching native suggestions for $prefix", e)
            emptyList()
        }
    }

    /**
     * Queries the native dictionary engine for next-word suggestions (bigram/trigram predictions) given the last typed word.
     */
    fun getNextWordSuggestions(
        language: String,
        lastWord: String?,
        limit: Int = 3
    ): List<String> {
        val dict = getDictionary(language) ?: return emptyList()
        if (lastWord.isNullOrBlank()) return emptyList()

        return try {
            val wordInfo = NgramContext.WordInfo(lastWord)
            val ngramContext = NgramContext(wordInfo)
            val composedData = ComposedData.createForWord("")
            val suggestions = dict.getSuggestions(
                composedData,
                ngramContext,
                dummyProximityInfo.nativeProximityInfo, // proximityInfoHandle
                SettingsValuesForSuggestion(false, false),
                1, // sessionId
                1.0f, // weightForLocale
                null // inOutWeightOfLangModelVsSpatialModel
            )

            suggestions?.map { it.mWord }
                ?.filter { it.isNotBlank() }
                ?.take(limit)
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching native next-word suggestions for $lastWord", e)
            emptyList()
        }
    }

    /**
     * Closes and clears all loaded dictionaries.
     */
    @Synchronized
    fun close() {
        for (dict in loadedDicts.values) {
            dict.close()
        }
        loadedDicts.clear()
    }
}
