// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.models.ScribeState

/**
 * Manages the current state transitions and state flags for the Scribe keyboard command system.
 */
class KeyboardStateManager {
    /**
     * The active state of the keyboard command system.
     */
    var currentState: ScribeState = ScribeState.IDLE

    /**
     * Records the previous command source when an invalid state occurs.
     */
    var invalidCommandSource: ScribeState = ScribeState.IDLE

    /**
     * Checks if the keyboard is currently in the `IDLE` state.
     */
    val isIdle: Boolean
        get() = currentState == ScribeState.IDLE

    /**
     * Checks if the keyboard is currently in the `SELECT_COMMAND` state.
     */
    val isSelectCommand: Boolean
        get() = currentState == ScribeState.SELECT_COMMAND

    /**
     * Checks if a command bar mode (translate, conjugate, plural, etc.) is active.
     */
    val isCommandBarActive: Boolean
        get() = currentState != ScribeState.IDLE && currentState != ScribeState.SELECT_COMMAND

    /**
     * Checks if the keyboard is currently in the `INVALID` state.
     */
    val isInvalid: Boolean
        get() = currentState == ScribeState.INVALID

    /**
     * Checks if the keyboard is currently in the `ALREADY_PLURAL` state.
     */
    val isAlreadyPlural: Boolean
        get() = currentState == ScribeState.ALREADY_PLURAL

    /**
     * Transitions the keyboard state to [newState].
     */
    fun moveToState(newState: ScribeState) {
        currentState = newState
    }

    /**
     * Resets the keyboard state to `IDLE`.
     */
    fun moveToIdle() {
        currentState = ScribeState.IDLE
    }

    /**
     * Transitions the keyboard to the `INVALID` state and records the [source] state.
     */
    fun setInvalidState(source: ScribeState) {
        invalidCommandSource = source
        currentState = ScribeState.INVALID
    }

    /**
     * Resets both [currentState] and [invalidCommandSource] to `IDLE`.
     */
    fun reset() {
        currentState = ScribeState.IDLE
        invalidCommandSource = ScribeState.IDLE
    }
}
