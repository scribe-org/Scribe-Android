// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.models

/**
 * Represents the various states of the Scribe keyboard command system.
 */
enum class ScribeState {
    IDLE,
    SELECT_COMMAND,
    TRANSLATE,
    CONJUGATE,
    PLURAL,
    SELECT_VERB_CONJUNCTION,
    INVALID,
    ALREADY_PLURAL,
}
