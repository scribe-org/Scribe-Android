// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.content.Context
import android.util.Log
import be.scri.data.remote.RetrofitClient
import be.scri.data.remote.SqlIdentifier
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads, deserializes, downloads, and saves contract data from local storage or network.
 * Contracts are stored as `<lang>.yaml`; JSON responses are stored as-is since JSON is valid YAML.
 * @param context The application context.
 */
class ContractDataLoader(
    private val context: Context,
) {
    companion object {
        private const val TAG = "ContractDataLoader"
        private const val CONTRACTS_DIR = "data-contracts"
        private val yaml =
            Yaml(
                configuration = YamlConfiguration(strictMode = false),
            )

        // Languages with a background download in flight, so repeated keyboard loads don't stack requests.
        private val pendingDownloads = ConcurrentHashMap.newKeySet<String>()
    }

    private fun contractFile(language: String) = File(File(context.filesDir, CONTRACTS_DIR), "${language.lowercase()}.yaml")

    /**
     * Loads and deserializes the locally saved data contract for the specified language.
     * Gracefully handles file-not-found and parsing errors by returning null.
     *
     * @param language The language code (e.g., "DE", "EN") used to determine the filename (e.g., "de.yaml").
     *
     * @return The decoded [DataContract] object if successful, or `null`
     * if the file does not exist or cannot be parsed.
     */
    fun loadContract(language: String): DataContract? {
        val file = contractFile(language)
        if (!file.isFile) {
            Log.e(TAG, "No local contract file for: ${language.lowercase()}")
            return null
        }
        return try {
            parseContract(file.readText())
        } catch (e: IOException) {
            Log.e(TAG, "Error reading local contract file: ${file.path}", e)
            null
        }
    }

    /**
     * Parses a YAML or JSON string into a [DataContract].
     *
     * @param content The YAML or JSON content to decode.
     * @return The decoded [DataContract] object, or `null` if parsing fails.
     */
    fun parseContract(content: String): DataContract? =
        try {
            yaml.decodeFromString(DataContract.serializer(), content)
        } catch (e: SerializationException) {
            Log.e(TAG, "Error parsing contract content", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error parsing contract content", e)
            null
        }

    /**
     * Validates that all database column names specified in [contract] are safe SQL identifiers.
     * Prevents SQL injection vulnerabilities when dynamic queries are formed.
     *
     * @param contract The [DataContract] to inspect.
     * @return True if all column names pass [SqlIdentifier.isValid], false otherwise.
     */
    fun validateContract(contract: DataContract): Boolean {
        for ((_, pair) in contract.nouns.numbers) {
            if (!SqlIdentifier.isValid(pair.singular) || !SqlIdentifier.isValid(pair.plural)) {
                Log.e(TAG, "Invalid column name in contract numbers: '${pair.singular}', '${pair.plural}'")
                return false
            }
        }
        val genderColumns =
            contract.nouns.genders.canonical +
                contract.nouns.genders.feminines +
                contract.nouns.genders.masculines +
                contract.nouns.genders.commons +
                contract.nouns.genders.neuters
        for (col in genderColumns) {
            if (!SqlIdentifier.isValid(col)) {
                Log.e(TAG, "Invalid column name in contract genders: '$col'")
                return false
            }
        }
        for ((_, tenseGroup) in contract.verbs.conjugations) {
            for ((_, category) in tenseGroup.tenses) {
                for ((_, form) in category.tenseForms) {
                    // Bracketed words are checked where they are used in ConjugateDataManager.parseComplexForm.
                    val rawCol = form.value.replace(Regex("""[\[{].*?[]}]"""), "").trim()
                    if (rawCol.isNotEmpty() && !SqlIdentifier.isValid(rawCol)) {
                        Log.e(TAG, "Invalid column name in contract verbs: '$rawCol'")
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Validates and saves contract content (YAML or JSON) to local app storage.
     * Content is parse-checked and SQL-identifier-validated before writing.
     * Writing uses a uniquely named temporary file and atomic rename so a failed, invalid
     * or concurrent write cannot corrupt or overwrite an existing working contract.
     *
     * @param language The language code (e.g., "DE", "EN").
     * @param content The contract string content to persist.
     * @return True if validated and saved successfully, false otherwise.
     */
    fun saveContract(
        language: String,
        content: String,
    ): Boolean {
        val parsedContract = parseContract(content)
        if (parsedContract == null) {
            Log.e(TAG, "Failed to parse contract content for $language before saving")
            return false
        }
        if (!validateContract(parsedContract)) {
            Log.e(TAG, "Contract content for $language contains invalid SQL identifiers")
            return false
        }

        val dir = File(context.filesDir, CONTRACTS_DIR)
        if (!dir.mkdirs() && !dir.isDirectory) {
            Log.e(TAG, "Failed to create directory: ${dir.path}")
            return false
        }
        val targetFile = contractFile(language)

        return try {
            val tempFile = File.createTempFile(targetFile.name, ".tmp", dir)
            try {
                tempFile.writeText(content)
                tempFile.renameTo(targetFile).also { renamed ->
                    if (!renamed) Log.e(TAG, "Failed to move temp contract file to target file for $language")
                }
            } finally {
                tempFile.delete()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write contract to temp file for $language", e)
            false
        }
    }

    /**
     * Downloads and saves the data contract for the specified language from Scribe-Server.
     *
     * @param language The language code (e.g., "DE", "EN").
     * @return True if downloaded and saved successfully, false otherwise.
     */
    suspend fun downloadAndSaveContract(language: String): Boolean {
        val lang = language.lowercase()
        return try {
            val response = RetrofitClient.apiService.getContract(lang)
            val content = response.string()
            saveContract(lang, content)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to download and save contract for $lang", e)
            false
        } catch (e: HttpException) {
            Log.e(TAG, "Server returned error while fetching contract for $lang", e)
            false
        }
    }

    /**
     * Loads the local contract for [language], downloading it first if it is missing or unreadable.
     *
     * @param language The language code (e.g., "DE", "EN").
     * @return The decoded [DataContract], or `null` if it could not be loaded or downloaded.
     */
    suspend fun loadOrDownloadContract(language: String): DataContract? =
        loadContract(language) ?: if (downloadAndSaveContract(language)) loadContract(language) else null

    /**
     * Downloads the contract for [language] in the background if its language database is already
     * downloaded, so users who downloaded data before contracts existed get one on their next load.
     *
     * @param language The language code (e.g., "DE", "EN").
     */
    fun downloadMissingContractInBackground(language: String) {
        val lang = language.lowercase()
        val hasDatabase = context.getDatabasePath("${lang.uppercase()}LanguageData.sqlite").exists()
        if (!hasDatabase || !pendingDownloads.add(lang)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loadOrDownloadContract(lang)
            } finally {
                pendingDownloads.remove(lang)
            }
        }
    }
}
