// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.content.Context
import android.util.Log
import be.scri.data.remote.RetrofitClient
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.File
import java.io.IOException

/**
 * Loads, deserializes, downloads, and saves contract data from local storage, assets, or network.
 * @param context The application context.
 */
class ContractDataLoader(
    private val context: Context,
) {
    companion object {
        private const val TAG = "ContractDataLoader"
        private const val CONTRACTS_DIR = "data-contracts"
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        private val yaml =
            Yaml(
                configuration = YamlConfiguration(strictMode = false),
            )
    }

    /**
     * Checks if a contract file exists in local app storage for the specified language.
     *
     * @param language The language code (e.g., "DE", "EN").
     * @return True if a local .yaml or .json contract file exists, false otherwise.
     */
    fun hasContract(language: String): Boolean {
        val lang = language.lowercase()
        val dir = File(context.filesDir, CONTRACTS_DIR)
        return File(dir, "$lang.yaml").exists() || File(dir, "$lang.json").exists()
    }

    /**
     * Loads and deserializes a data contract for the specified language.
     * Checks internal app storage first, falling back to assets.
     * Gracefully handles file-not-found and parsing errors by returning null.
     *
     * @param language The language code (e.g., "DE", "EN") used to determine the filename (e.g., "de.yaml" or "de.json").
     *
     * @return The decoded [DataContract] object if successful, or `null`
     * if the file does not exist or cannot be parsed.
     */
    fun loadContract(language: String): DataContract? {
        val lang = language.lowercase()
        Log.d(TAG, "Attempting to load contract for language: $lang")

        val dir = File(context.filesDir, CONTRACTS_DIR)
        val localFiles = listOf(File(dir, "$lang.yaml"), File(dir, "$lang.json"))
        for (localFile in localFiles) {
            if (localFile.exists() && localFile.isFile) {
                try {
                    val parsed = parseContract(localFile.readText())
                    if (parsed != null) return parsed
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading local contract file: ${localFile.path}", e)
                }
            }
        }

        val assetExtensions = listOf("yaml", "json")
        for (ext in assetExtensions) {
            val assetPath = "$CONTRACTS_DIR/$lang.$ext"
            try {
                context.assets.open(assetPath).use { contractFile ->
                    val content = contractFile.bufferedReader().readText()
                    val parsed = parseContract(content)
                    if (parsed != null) return parsed
                }
            } catch (_: IOException) {
                // Asset not found, try next extension.
            }
        }

        Log.e(TAG, "Error loading contract file for: $lang. It may not exist.")
        return null
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
        } catch (_: YamlException) {
            parseContractAsJson(content)
        } catch (_: SerializationException) {
            parseContractAsJson(content)
        } catch (_: IllegalArgumentException) {
            parseContractAsJson(content)
        }

    private fun parseContractAsJson(content: String): DataContract? =
        try {
            json.decodeFromString(DataContract.serializer(), content)
        } catch (e: SerializationException) {
            Log.e(TAG, "Error parsing contract content as JSON", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error parsing contract content as JSON", e)
            null
        }

    /**
     * Saves contract content (YAML or JSON) to local app storage.
     *
     * @param language The language code (e.g., "DE", "EN").
     * @param yamlContent The contract string content to persist.
     * @return True if saved successfully, false otherwise.
     */
    fun saveContract(
        language: String,
        yamlContent: String,
    ): Boolean {
        val dir = File(context.filesDir, CONTRACTS_DIR)
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Failed to create directory: ${dir.path}")
            return false
        }
        val lang = language.lowercase()
        val isJson = yamlContent.trimStart().startsWith("{")
        val extension = if (isJson) "json" else "yaml"
        val file = File(dir, "$lang.$extension")
        return try {
            file.writeText(yamlContent)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write contract file for $language", e)
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
}
