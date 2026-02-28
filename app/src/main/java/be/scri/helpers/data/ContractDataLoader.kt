// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.content.Context
import android.util.Log
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException
import java.io.IOException

/**
 * Loads and deserializes contract data from assets.
 * @param context The application context.
 */
class ContractDataLoader(
    private val context: Context,
) {
    /**
     * Loads and deserializes a data contract from a YAML file in the assets folder.
     * It gracefully handles file-not-found and YAML parsing errors by returning null.
     *
     * @param language The language code (e.g., "DE", "EN") used to determine the filename (e.g., "de.yaml").
     *
     * @return The decoded [DataContract] object if successful, or `null`
     * if the file does not exist or cannot be parsed.
     */
    fun loadContract(language: String): DataContract? {
        val contractName = "${language.lowercase()}.yaml"
        Log.i("ContractDataLoader", "Attempting to load contract: $contractName")

        return try {
            context.assets.open("data-contracts/$contractName").use { contractFile ->
                val content = contractFile.bufferedReader().readText()
                val yaml =
                    Yaml(
                        configuration = YamlConfiguration(strictMode = false),
                    )
                yaml.decodeFromString(DataContract.serializer(), content)
            }
        } catch (e: IOException) {
            Log.e("ContractDataLoader", "Error loading contract file: $contractName. It may not exist.", e)
            null
        } catch (e: YamlException) {
            Log.e("ContractDataLoader", "Error parsing YAML for contract: $contractName", e)
            null
        }
    }
}
