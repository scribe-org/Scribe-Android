// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import be.scri.helpers.DatabaseFileManager
import be.scri.helpers.data.columnExists
import be.scri.helpers.data.tableExists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Data class representing a search result for a verb.
 */
data class ConjugateSearchResult(
    val verb: String,
    val languageAlias: String,
)

/**
 * ViewModel to manage verb search and recently conjugated items on the Conjugate screen.
 */
class ConjugateViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("scribe_conjugate_search_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ConjugateSearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _recentlyConjugated = MutableStateFlow<List<ConjugateSearchResult>>(emptyList())
    val recentlyConjugated = _recentlyConjugated.asStateFlow()

    init {
        loadRecentlyConjugated()
    }

    /**
     * Updates the search query and triggers database search if not blank.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            performSearch(query)
        }
    }

    /**
     * Clears the current search query.
     */
    fun clearSearchQuery() {
        onSearchQueryChanged("")
    }

    private fun performSearch(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<ConjugateSearchResult>()
            val fileManager = DatabaseFileManager(getApplication())
            val aliases = listOf("EN", "FR", "DE", "IT", "PT", "RU", "ES", "SV")

            for (alias in aliases) {
                val db = fileManager.getConjugateDatabase(alias) ?: continue

                try {
                    val columnName = if (alias == "SV") "verb" else "infinitive"
                    if (db.tableExists("verbs") && db.columnExists("verbs", columnName)) {
                        db
                            .rawQuery(
                                "SELECT DISTINCT $columnName FROM verbs WHERE $columnName LIKE ? LIMIT 10",
                                arrayOf("$query%"),
                            ).use { cursor ->
                                val colIndex = cursor.getColumnIndex(columnName)
                                if (colIndex != -1) {
                                    while (cursor.moveToNext()) {
                                        val verb = cursor.getString(colIndex)
                                        if (!verb.isNullOrBlank()) {
                                            results.add(ConjugateSearchResult(verb, alias))
                                        }
                                    }
                                }
                            }
                    }
                } catch (e: android.database.sqlite.SQLiteException) {
                    Log.e("ConjugateViewModel", "Error searching db for $alias", e)
                } catch (e: IllegalStateException) {
                    Log.e("ConjugateViewModel", "Error searching db for $alias", e)
                } finally {
                    try {
                        db.close()
                    } catch (e: android.database.sqlite.SQLiteException) {
                        Log.e("ConjugateViewModel", "Error closing db for $alias", e)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (_searchQuery.value == query) {
                    _searchResults.value = results.distinctBy { it.verb.lowercase() + "_" + it.languageAlias }
                }
            }
        }
    }

    /**
     * Triggers when a verb search result or recently conjugated item is clicked.
     */
    fun onVerbSelected(result: ConjugateSearchResult) {
        addToRecentlyConjugated(result)
        clearSearchQuery()
    }

    private fun addToRecentlyConjugated(item: ConjugateSearchResult) {
        val currentList = _recentlyConjugated.value.toMutableList()
        currentList.removeAll { it.verb.equals(item.verb, ignoreCase = true) && it.languageAlias == item.languageAlias }
        currentList.add(0, item)

        val updatedList = currentList.take(15)
        _recentlyConjugated.value = updatedList
        saveRecentlyConjugated(updatedList)
    }

    /**
     * Clears all items in the recently conjugated list.
     */
    fun clearAllRecentlyConjugated() {
        _recentlyConjugated.value = emptyList()
        prefs.edit().remove("recently_conjugated_list").apply()
    }

    private fun loadRecentlyConjugated() {
        val serialized = prefs.getString("recently_conjugated_list", null) ?: return
        if (serialized.isBlank()) return

        val list =
            serialized.split(";").mapNotNull { part ->
                val tokens = part.split(",")
                if (tokens.size == 2) {
                    ConjugateSearchResult(tokens[0], tokens[1])
                } else {
                    null
                }
            }
        _recentlyConjugated.value = list
    }

    private fun saveRecentlyConjugated(list: List<ConjugateSearchResult>) {
        val serialized = list.joinToString(";") { "${it.verb},${it.languageAlias}" }
        prefs.edit().putString("recently_conjugated_list", serialized).apply()
    }
}
