
import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.IOException

class ContractDataLoader(
    private val context: Context,
) {
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
