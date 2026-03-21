// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.about

import be.scri.ui.common.bottombar.BottomBarScreen
import kotlin.test.Test
import kotlin.test.assertEquals

class BottomBarScreenTest {
    @Test
    fun getScreens_WhenIsConjugateTrue_ReturnsConjugateScreens() {
        val isConjugate = true

        val result = BottomBarScreen.getScreens(isConjugate)

        assertEquals(
            listOf(
                BottomBarScreen.Conjugate,
                BottomBarScreen.Settings,
                BottomBarScreen.About,
            ),
            result,
        )
    }

    @Test
    fun getScreens_WhenIsConjugateFalse_ReturnsInstallationScreens() {
        val isConjugate = false

        val result = BottomBarScreen.getScreens(isConjugate)

        assertEquals(
            listOf(
                BottomBarScreen.Installation,
                BottomBarScreen.Settings,
                BottomBarScreen.About,
            ),
            result,
        )
    }
}
