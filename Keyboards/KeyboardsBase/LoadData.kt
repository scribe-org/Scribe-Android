//
//  LoadData.kt
//
//  Function for loading in data to the keyboards.
//

/// Loads a JSON file that contains grammatical information into a dictionary.
///
/// - Parameters
///  - filename: the name of the JSON file to be loaded.
internal fun loadJSONToDict(fileName: String) : Map<String, Any>? {
    val url = Bundle.main.url(forResource = fileName, withExtension = "json")
    if (url != null) {
        do {
            val data = Data(contentsOf = url)
            val jsonData = JSONSerialization.jsonObject(with = data)
            return jsonData as? Map<String, Any>
        } catch {
            print("error:${error}")
        }
    }
    return null
}
