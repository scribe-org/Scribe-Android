// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.Context
import android.content.SharedPreferences
import be.scri.helpers.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class RegularDataCheckPreferenceTest {
    @Test
    fun regularlyCheckForDataUpdatesDefaultsToFalse() {
        val prefs = mockk<SharedPreferences>()
        val context = mockk<Context>()
        every { context.getSharedPreferences(PreferencesHelper.SCRIBE_PREFS, Context.MODE_PRIVATE) } returns prefs
        every { prefs.getBoolean("regularly_check_for_data_updates", false) } returns false

        assertFalse(PreferencesHelper.getRegularlyCheckForDataUpdates(context))

        verify { prefs.getBoolean("regularly_check_for_data_updates", false) }
    }
}
