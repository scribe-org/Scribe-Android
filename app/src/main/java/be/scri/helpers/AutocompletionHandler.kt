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

    /**
     * Processes the current word for autocompletion.
     *
     * This function is called whenever the user types.
     * It cancels any pending autocomplete request and schedules a new one
     * after a short delay to prevent excessive lookups.
     *
     * @param currentWord The word currently being typed by the user.
     */
    fun processAutocomplete(currentWord: String?) {
        autocompleteRunnable?.let { handler.removeCallbacks(it) }

        autocompleteRunnable =
            Runnable {
                if (ime.currentState != ScribeState.IDLE) {
                    ime.clearAutocomplete()
                    return@Runnable
                }

                if (currentWord.isNullOrEmpty()) {
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

    /**
     * Immediately cancels any scheduled autocomplete task
     * and clears autocomplete suggestions from the keyboard.
     */
    fun clearAutocomplete() {
        autocompleteRunnable?.let { handler.removeCallbacks(it) }
        ime.clearAutocomplete()
    }
}
