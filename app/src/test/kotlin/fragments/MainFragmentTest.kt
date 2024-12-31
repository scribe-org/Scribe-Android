/**
 * Testing for MainFragment.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import be.scri.fragments.MainFragment
import be.scri.helpers.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MainFragmentTest {
    private lateinit var mainFragment: MainFragment
    private val mockContext: Context = mockk()
    private val mockSharedPreferences: SharedPreferences = mockk()
    private val mockUiModeManager: UiModeManager = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        mainFragment = MainFragment()
        mockkStatic(PreferencesHelper::class)

        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.getBoolean(eq("dark_mode"), any()) } returns true
        every { mockContext.getSystemService(Context.UI_MODE_SERVICE) } returns mockUiModeManager
        every { mockUiModeManager.nightMode } returns UiModeManager.MODE_NIGHT_YES
    }

    @Test
    fun `GIVEN dark mode enabled in preferences WHEN getDarkModePreference is called THEN returns true`() {
        every { mockContext.getSharedPreferences("preferences", Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockSharedPreferences.getBoolean("dark_mode", false) } returns true
        val result = mainFragment.getDarkModePreference(mockContext)
        assertTrue(result)
    }
}
