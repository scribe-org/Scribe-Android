// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.services.GeneralKeyboardIME
import be.scri.views.KeyboardView
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShiftHandlerTest {
    private val ime = mockk<GeneralKeyboardIME>(relaxed = true)
    private val keyboardView = mockk<KeyboardView>(relaxed = true)
    private val keyboard = mockk<KeyboardBase>(relaxed = true)

    @Before
    fun setUp() {
        every { keyboardView.mKeyboard } returns keyboard
        every { ime.keyboardLetters } returns 0
        every { ime.keyboardSymbols } returns 1
        every { ime.keyboardSymbolShift } returns 2
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun lastShiftPressTS_readsAndWritesToIme() {
        every { ime.lastShiftPressTS } returns 12345L
        val handler = ShiftHandler(ime)

        assertEquals(12345L, handler.lastShiftPressTS)

        handler.lastShiftPressTS = 67890L
        verify { ime.lastShiftPressTS = 67890L }
    }

    @Test
    fun handleKeyboardLetters_singleTapFromOff_setsShiftOnOneChar() {
        val currentTime = 1000L
        every { keyboard.mShiftState } returns SHIFT_OFF
        every { ime.lastShiftPressTS } returns 0L

        val handler = ShiftHandler(ime, timeProvider = { currentTime })
        handler.handleKeyboardLetters(keyboardMode = 0, keyboardView = keyboardView)

        verify { keyboardView.setShifted(SHIFT_ON_ONE_CHAR) }
        verify { ime.lastShiftPressTS = 1000L }
    }

    @Test
    fun handleKeyboardLetters_doubleTapWithin500ms_setsShiftOnPermanent() {
        val currentTime = 1000L
        every { keyboard.mShiftState } returns SHIFT_ON_ONE_CHAR
        every { ime.lastShiftPressTS } returns 800L

        val handler = ShiftHandler(ime, timeProvider = { currentTime })
        handler.handleKeyboardLetters(keyboardMode = 0, keyboardView = keyboardView)

        verify { keyboardView.setShifted(SHIFT_ON_PERMANENT) }
        verify { ime.lastShiftPressTS = 1000L }
    }

    @Test
    fun handleKeyboardLetters_tapAfterReset_setsShiftOff() {
        val currentTime = 2000L
        every { keyboard.mShiftState } returns SHIFT_ON_ONE_CHAR
        every { ime.lastShiftPressTS } returns 0L

        val handler = ShiftHandler(ime, timeProvider = { currentTime })
        handler.handleKeyboardLetters(keyboardMode = 0, keyboardView = keyboardView)

        verify { keyboardView.setShifted(SHIFT_OFF) }
        verify { ime.lastShiftPressTS = 2000L }
    }

    @Test
    fun handleKeyboardLetters_permanentShiftState_togglesShiftOff() {
        val currentTime = 1500L
        every { keyboard.mShiftState } returns SHIFT_ON_PERMANENT
        every { ime.lastShiftPressTS } returns 1000L

        val handler = ShiftHandler(ime, timeProvider = { currentTime })
        handler.handleKeyboardLetters(keyboardMode = 0, keyboardView = keyboardView)

        verify { keyboardView.setShifted(SHIFT_OFF) }
        verify { ime.lastShiftPressTS = 1500L }
    }

    @Test
    fun handleKeyboardLetters_tapAfterSpeedLimit_setsShiftOff() {
        val currentTime = 2000L
        every { keyboard.mShiftState } returns SHIFT_ON_ONE_CHAR
        every { ime.lastShiftPressTS } returns 1200L

        val handler = ShiftHandler(ime, timeProvider = { currentTime })
        handler.handleKeyboardLetters(keyboardMode = 0, keyboardView = keyboardView)

        verify { keyboardView.setShifted(SHIFT_OFF) }
        verify { ime.lastShiftPressTS = 2000L }
    }
}
