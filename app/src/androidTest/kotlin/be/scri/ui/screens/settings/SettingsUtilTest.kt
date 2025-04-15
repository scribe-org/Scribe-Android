package be.scri.ui.screens.settings

import android.content.Context
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsUtilTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSetLightDarkMode_SetsDarkModeCorrectly() {
        SettingsUtil.setLightDarkMode(true, context)

        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedPref = sharedPref.getBoolean("dark_mode", false)
        assertTrue(savedPref)

        val currentMode = AppCompatDelegate.getDefaultNightMode()
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, currentMode)
    }

    @Test
    fun testSetLightDarkMode_SetsLightModeCorrectly() {
        SettingsUtil.setLightDarkMode(false, context)

        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedPref = sharedPref.getBoolean("dark_mode", true)
        assertFalse(savedPref)

        val currentMode = AppCompatDelegate.getDefaultNightMode()
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, currentMode)
    }

    @Test
    fun testNavigateToKeyboardSettings_LaunchesCorrectIntent() {
        SettingsUtil.navigateToKeyboardSettings(context)

        Intents.intended(hasAction(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    @Test
    fun testGetLocalizedLanguageName_English() {
        val result = SettingsUtil.getLocalizedLanguageName("English")

        assertEquals("English", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_French() {
        val result = SettingsUtil.getLocalizedLanguageName("French")

        assertEquals("French", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_German() {
        val result = SettingsUtil.getLocalizedLanguageName("German")

        assertEquals("German", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_Russian() {
        val result = SettingsUtil.getLocalizedLanguageName("Russian")

        assertEquals("Russian", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_Spanish() {
        val result = SettingsUtil.getLocalizedLanguageName("Spanish")

        assertEquals("Spanish", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_Italian() {
        val result = SettingsUtil.getLocalizedLanguageName("Italian")

        assertEquals("Italian", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_Portuguese() {
        val result = SettingsUtil.getLocalizedLanguageName("Portuguese")

        assertEquals("Portuguese", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_Swedish() {
        val result = SettingsUtil.getLocalizedLanguageName("Swedish")

        assertEquals("Swedish", context.getString(result))
    }

    @Test
    fun testGetLocalizedLanguageName_Undeclared() {
        val result = SettingsUtil.getLocalizedLanguageName("Yoruba")

        assertEquals("Language", context.getString(result))
    }
}
