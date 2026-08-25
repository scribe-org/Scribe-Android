// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeyboardThemeManagerTest {
    private lateinit var themeManager: KeyboardThemeManager

    @Before
    fun setUp() {
        themeManager = KeyboardThemeManager()
    }

    @Test
    fun isLightColor_whiteColor_returnsTrue() {
        assertTrue(themeManager.isLightColor(Color.WHITE))
    }

    @Test
    fun isLightColor_blackColor_returnsFalse() {
        assertFalse(themeManager.isLightColor(Color.BLACK))
    }

    @Test
    fun isLightColor_lightGrayColor_returnsTrue() {
        assertTrue(themeManager.isLightColor(Color.LTGRAY))
    }

    @Test
    fun isLightColor_darkGrayColor_returnsFalse() {
        assertFalse(themeManager.isLightColor(Color.DKGRAY))
    }
}
