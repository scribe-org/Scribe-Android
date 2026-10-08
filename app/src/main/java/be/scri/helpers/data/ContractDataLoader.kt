// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.content.Context
import android.util.Log
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException
import java.io.File
import java.io.IOException

/**
 * Loads and deserializes contract data from local storage or assets.
 * @param context The application context.
 */
class ContractDataLoader(
    private val context: Context,
) {
    companion object {
        private const val TAG = "ContractDataLoader"
        private const val CONTRACTS_DIR = "data-contracts"
    }

    /**
     * Loads and deserializes a data contract for the specified language.
     * Checks internal app storage first, falling back to assets.
     * Gracefully handles file-not-found and YAML parsing errors by returning null.
     *
     * @param language The language code (e.g., "DE", "EN") used to determine the filename (e.g., "de.yaml").
     *
     * @return The decoded [DataContract] object if successful, or `null`
     * if the file does not exist or cannot be parsed.
     */
    fun loadContract(language: String): DataContract? {
        val contractName = "${language.lowercase()}.yaml"
        Log.d(TAG, "Attempting to load contract: $contractName")

        val localFile = File(File(context.filesDir, CONTRACTS_DIR), contractName)
        if (localFile.exists() && localFile.isFile) {
            return try {
                parseContract(localFile.readText())
            } catch (e: IOException) {
                Log.e(TAG, "Error reading local contract file: ${localFile.path}", e)
                null
            }
        }

        return try {
            context.assets.open("$CONTRACTS_DIR/$contractName").use { contractFile ->
                val content = contractFile.bufferedReader().readText()
                parseContract(content)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading contract file: $contractName. It may not exist.", e)
            null
        }
    }

    /**
     * Parses a YAML string into a [DataContract].
     *
     * @param content The YAML content to decode.
     * @return The decoded [DataContract] object, or `null` if parsing fails.
     */
    fun parseContract(content: String): DataContract? =
        try {
            val yaml =
                Yaml(
                    configuration = YamlConfiguration(strictMode = false),
                )
            yaml.decodeFromString(DataContract.serializer(), content)
        } catch (e: YamlException) {
            Log.e(TAG, "Error parsing YAML contract", e)
            null
        }

    /**
     * Saves a contract YAML string to local app storage.
     *
     * @param language The language code (e.g., "DE", "EN").
     * @param yamlContent The YAML string content to persist.
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
        val file = File(dir, "${language.lowercase()}.yaml")
        return try {
            file.writeText(yamlContent)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write contract file for $language", e)
            false
        }
    }
}
