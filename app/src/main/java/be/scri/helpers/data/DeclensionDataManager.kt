package be.scri.helpers.data

import DataContract
import DeclensionNode
import be.scri.latin.utils.Log

class DeclensionDataManager {
    fun getDeclensions(
        language: String,
        yamlData: DataContract?,
    ): LinkedHashMap<String, List<DeclensionNode>> {
        Log.d(TAG, "Loading declensions for language $language")
        val returnCases = mutableMapOf<String, List<DeclensionNode>>()

        val declensionsDictionary = yamlData?.declensions ?: return LinkedHashMap(returnCases)

        val sortedDeclensionKeys = declensionsDictionary.keys.sorted()

        for (key in sortedDeclensionKeys) {
            val declension = declensionsDictionary[key]
            val title = declension?.title ?: declension?.sectionTitle
            val forms = declension?.declensionForms

            if (title !== null && forms !== null) {
                val options = parseForms(forms)
                returnCases[title] = options
            }
        }

        return LinkedHashMap(returnCases)
    }

    private fun parseForms(forms: Map<Int, DeclensionNode>): List<DeclensionNode> {
        val options = mutableListOf<DeclensionNode>()

        for (key in forms.keys.sorted()) {
            val node = forms[key] ?: continue
            if (node.value != null || !node.declensionForms.isNullOrEmpty()) {
                options.add(node)
            }
        }
        return options
    }

    companion object {
        const val TAG = "DeclensionManager"
    }
}
