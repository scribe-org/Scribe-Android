import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import be.scri.ui.screens.InstallationScreen
import be.scri.ui.theme.ScribeTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.core.app.ApplicationProvider

class MainFragmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    @Test
    fun testDarkModeIsApplied() {
        sharedPreferences.edit().putInt("dark_mode", AppCompatDelegate.MODE_NIGHT_YES).apply()
        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = true) {
                InstallationScreen()
            }
        }
        composeTestRule.onNodeWithTag("backgroundContainer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("keyboardSettingsCard").assertIsDisplayed()
    }

    @Test
    fun testLightModeIsApplied() {
        sharedPreferences.edit().putInt("dark_mode", AppCompatDelegate.MODE_NIGHT_NO).apply()
        composeTestRule.setContent {
            ScribeTheme(useDarkTheme = false) {
                InstallationScreen()
            }
        }

        composeTestRule.onNodeWithTag("backgroundContainer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("keyboardSettingsCard").assertIsDisplayed()
    }
}
