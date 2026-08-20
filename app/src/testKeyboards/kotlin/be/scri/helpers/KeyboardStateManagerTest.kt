// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.models.ScribeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeyboardStateManagerTest {

    private lateinit var stateManager: KeyboardStateManager

    @Before
    fun setUp() {
        stateManager = KeyboardStateManager()
    }

    @Test
    fun initialState_isIdle() {
        assertEquals(ScribeState.IDLE, stateManager.currentState)
        assertEquals(ScribeState.IDLE, stateManager.invalidCommandSource)
        assertTrue(stateManager.isIdle)
        assertFalse(stateManager.isSelectCommand)
        assertFalse(stateManager.isCommandBarActive)
        assertFalse(stateManager.isInvalid)
        assertFalse(stateManager.isAlreadyPlural)
    }

    @Test
    fun moveToState_updatesCurrentState() {
        stateManager.moveToState(ScribeState.TRANSLATE)
        assertEquals(ScribeState.TRANSLATE, stateManager.currentState)
        assertFalse(stateManager.isIdle)
        assertTrue(stateManager.isCommandBarActive)

        stateManager.moveToState(ScribeState.SELECT_COMMAND)
        assertEquals(ScribeState.SELECT_COMMAND, stateManager.currentState)
        assertTrue(stateManager.isSelectCommand)
        assertFalse(stateManager.isCommandBarActive)
    }

    @Test
    fun moveToIdle_resetsToIdle() {
        stateManager.moveToState(ScribeState.CONJUGATE)
        stateManager.moveToIdle()
        assertEquals(ScribeState.IDLE, stateManager.currentState)
        assertTrue(stateManager.isIdle)
    }

    @Test
    fun setInvalidState_setsInvalidStateAndRecordsSource() {
        stateManager.moveToState(ScribeState.PLURAL)
        stateManager.setInvalidState(ScribeState.PLURAL)

        assertEquals(ScribeState.INVALID, stateManager.currentState)
        assertEquals(ScribeState.PLURAL, stateManager.invalidCommandSource)
        assertTrue(stateManager.isInvalid)
    }

    @Test
    fun isAlreadyPlural_returnsTrueWhenStateIsAlreadyPlural() {
        stateManager.moveToState(ScribeState.ALREADY_PLURAL)
        assertTrue(stateManager.isAlreadyPlural)
    }

    @Test
    fun reset_resetsStateAndInvalidSourceToIdle() {
        stateManager.setInvalidState(ScribeState.TRANSLATE)
        stateManager.reset()

        assertEquals(ScribeState.IDLE, stateManager.currentState)
        assertEquals(ScribeState.IDLE, stateManager.invalidCommandSource)
        assertTrue(stateManager.isIdle)
    }
}
