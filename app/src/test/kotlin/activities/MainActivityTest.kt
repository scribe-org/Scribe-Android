import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MainActivityTest {
    @Test
    fun testApplyUserDarkModePreference_darkModeEnabled() {
        val context = mockk<Context>(relaxed = true)
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.getBoolean("dark_mode", any()) } returns true

        ActivityScenario.launch(MainActivity::class.java).onActivity { activity ->
            activity.applyUserDarkModePreference(context)
            assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
        }

        verify { sharedPreferences.getBoolean("dark_mode", any()) }
        unmockkAll()
    }

    @Test
    fun testApplyUserDarkModePreference_darkModeDisabled() {
        val context = mockk<Context>(relaxed = true)
        val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.getBoolean("dark_mode", any()) } returns false

        ActivityScenario.launch(MainActivity::class.java).onActivity { activity ->
            activity.applyUserDarkModePreference(context)
            assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())
        }

        verify { sharedPreferences.getBoolean("dark_mode", any()) }
        unmockkAll()
    }
}
