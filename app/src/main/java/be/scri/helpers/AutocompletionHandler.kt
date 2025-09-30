// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.os.Handler
import android.os.Looper
import be.scri.services.GeneralKeyboardIME
import be.scri.services.GeneralKeyboardIME.ScribeState

/**
 * Handles autocompletion when user is typing.
 *
 * @property ime The [GeneralKeyboardIME] instance this handler is associated with.
 */
class AutocompletionHandler(
    private val ime: GeneralKeyboardIME,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var autocompleteRunnable: Runnable? = null

    companion object {
        private const val AUTOCOMPLETE_DELAY_MS = 50L
    }

    fun processAutocomplete(currentWord: String?) {
        autocompleteRunnable?.let { handler.removeCallbacks(it) }

        autocompleteRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE) {
                    ime.clearAutocomplete()
                    return@Runnable
                }

                if (currentWord.isNullOrEmpty() || currentWord.length < 2) {
                    ime.clearAutocomplete()
                    return@Runnable
                }

                val completions = ime.getAutocompletions(currentWord, limit = 5)

                if (completions.isNotEmpty()) {
                    ime.updateAutocompleteSuggestions(completions)
                } else {
                    ime.clearAutocomplete()
                }
            }

        handler.postDelayed(autocompleteRunnable!!, AUTOCOMPLETE_DELAY_MS)
    }

    fun clearAutocomplete() {
        autocompleteRunnable?.let { handler.removeCallbacks(it) }
        ime.clearAutocomplete()
    }
}
