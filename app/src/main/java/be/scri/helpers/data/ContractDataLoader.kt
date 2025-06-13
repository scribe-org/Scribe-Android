// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import DataContract
import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Loads and deserializes contract data from assets.
 * @param context The application context.
 */
class ContractDataLoader(
    private val context: Context,
) {
    /**
     * Loads a data contract from an assets JSON file.
     * @param language The language code (e.g., "DE", "EN") to load the contract for.
     * @return The decoded [DataContract] object, or null on failure.
     */
    fun loadContract(language: String): DataContract? {
        val contractName = "${language.lowercase()}.json"
        Log.i("ContractDataLoader", "Attempting to load contract: $contractName")

        return try {
            val jsonParser = Json { ignoreUnknownKeys = true }
            context.assets.open("data-contracts/$contractName").use { contractFile ->
                val content = contractFile.bufferedReader().readText()
                jsonParser.decodeFromString<DataContract>(content)
            }
        } catch (e: IOException) {
            Log.e("ContractDataLoader", "Error loading contract file: $contractName. It may not exist.", e)
            null
        } catch (e: SerializationException) {
            Log.e("ContractDataLoader", "Error parsing JSON for contract: $contractName", e)
            null
        }
    }
}
