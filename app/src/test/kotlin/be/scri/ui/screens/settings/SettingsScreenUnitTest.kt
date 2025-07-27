// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.settings

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsScreenUnitTest {
    @Test
    fun `PaddingLarge has correct value`() {
        assertEquals(20.dp, Dimensions.PaddingLarge)
    }

    @Test
    fun `PaddingSmallXL has correct value`() {
        assertEquals(12.dp, Dimensions.PaddingSmallXL)
    }

    @Test
    fun `TextSizeExtraLarge has correct value`() {
        assertEquals(24.sp, Dimensions.TextSizeExtraLarge)
    }

    @Test
    fun `ElevationSmall has correct value`() {
        assertEquals(4.dp, Dimensions.ElevationSmall)
    }

    @Test
    fun `all dimension values are positive`() {
        assertTrue(Dimensions.PaddingLarge.value > 0)
        assertTrue(Dimensions.PaddingSmallXL.value > 0)
        assertTrue(Dimensions.TextSizeExtraLarge.value > 0)
        assertTrue(Dimensions.ElevationSmall.value > 0)
    }

    @Test
    fun `padding values are ordered correctly`() {
        assertTrue(Dimensions.PaddingLarge > Dimensions.PaddingSmallXL)
    }
}
