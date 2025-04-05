// SPDX-License-Identifier: GPL-3.0-or-later

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * A helper class to load and deserialize contract data from assets based on the specified language.
 */
class ContractDataLoader(
    private val context: Context,
) {
    /**
     * Loads a data contract JSON file from the assets based on the given language.
     * The file is expected to be located in the `assets/data-contracts` directory and named as `<language>.json`.
     *
     * @param language the language for which to load the data contract (e.g., "en", "fr")
     * @return the decoded [DataContract] object if successful; null otherwise
     */
    fun loadContract(language: String): DataContract? {
        val contractName = "${language.lowercase()}.json"
        Log.i("ALPHA", "This is the $language")

        return try {
            val json = Json { ignoreUnknownKeys = true }
            context.assets.open("data-contracts/$contractName").use { contractFile ->
                val content = contractFile.bufferedReader().readText()
                Log.i("ALPHA", content)
                json.decodeFromString<DataContract>(content).also {
                    Log.i("MY-TAG", it.toString())
                }
            }
        } catch (e: IOException) {
            Log.e("MY-TAG", "Error loading contract: $contractName", e)
            null
        }
    }
}
