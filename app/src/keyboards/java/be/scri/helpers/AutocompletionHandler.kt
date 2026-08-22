// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.os.Handler
import android.os.Looper
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME

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
        private const val AUTOCOMPLETE_DELAY_MS = 150L
        private const val MAX_COMPLETIONS = 2

        /**
         * Filters dictionary/engine [completions] down to the ones worth showing
         * alongside the word the user already typed: no duplicate of [typedWord],
         * capped at [MAX_COMPLETIONS] (the word itself takes the remaining slot).
         */
        internal fun buildCompletions(
            typedWord: String,
            completions: List<String>,
        ): List<String> =
            completions
                .filterNot { it.equals(typedWord, ignoreCase = true) }
                .take(MAX_COMPLETIONS)
    }

    /**
     * Processes the current word for autocompletion.
     *
     * This function is called whenever the user types. The word being typed is
     * shown immediately (it's already known, no lookup needed), while the
     * dictionary/engine completions are debounced to avoid excessive lookups.
     *
     * @param currentWord The word currently being typed by the user.
     */
    fun processAutocomplete(currentWord: String?) {
        autocompleteRunnable?.let { handler.removeCallbacks(it) }

        if (ime.currentState != ScribeState.IDLE || currentWord.isNullOrEmpty()) {
            ime.clearAutocomplete()
            return
        }

        ime.updateTypedWordSuggestion(currentWord)

        autocompleteRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE) return@Runnable

                val previousWord = ime.getPreviousWordBeforeCursor()
                val completions = ime.getAutocompletions(currentWord, previousWord, limit = 5)

                ime.updateAutocompleteCompletions(buildCompletions(currentWord, completions))
            }

        handler.postDelayed(autocompleteRunnable!!, AUTOCOMPLETE_DELAY_MS)
    }

    /**
     * Immediately cancels any scheduled autocomplete task
     * and clears autocomplete suggestions from the keyboard.
     */
    fun clearAutocomplete() {
        autocompleteRunnable?.let { handler.removeCallbacks(it) }
        ime.clearAutocomplete()
    }
}
