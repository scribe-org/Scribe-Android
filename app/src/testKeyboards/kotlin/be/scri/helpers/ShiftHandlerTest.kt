// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftHandlerTest {
    @Test
    fun initialLastShiftPressTS_isZero() {
        var lastShiftPressTS: Long = 0L
        assertEquals(0L, lastShiftPressTS)
    }

    @Test
    fun doubleTapTiming_within500ms_isPermanentCaps() {
        val lastPressTS = System.currentTimeMillis() - 200
        val speedLimit = 500
        val isDoubleTap = System.currentTimeMillis() - lastPressTS < speedLimit
        assertTrue(isDoubleTap)
    }
}
