import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.scri.fragments.MainFragment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFragmentTest {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = mockk()
        editor = mockk()
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.getBoolean("dark_mode", false) } returns true
    }

    @Test
    fun testApplyUserDarkModePreference_darkModeEnabled() {
        val fragmentScenario = FragmentScenario.launchInContainer(MainFragment::class.java)
        fragmentScenario.onFragment { fragment ->
            fragment.applyUserDarkModePreference()
            verify { editor.apply() }
            assert(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    @Test
    fun testApplyUserDarkModePreference_darkModeDisabled() {
        every { sharedPreferences.getBoolean("dark_mode", false) } returns false
        val fragmentScenario = FragmentScenario.launchInContainer(MainFragment::class.java)
        fragmentScenario.onFragment { fragment ->
            fragment.applyUserDarkModePreference()
            verify { editor.apply() }
            assert(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
