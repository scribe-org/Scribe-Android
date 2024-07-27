package be.scri.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import be.scri.BuildConfig
import be.scri.R
import be.scri.dialogs.ConfirmationAdvancedDialog
import be.scri.extensions.*
import be.scri.helpers.LICENSE_GSON
import kotlin.math.abs

class MainActivity : SimpleActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        applyUserDarkModePreference()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)
        gestureDetector = GestureDetector(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.installation

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.installation -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.info -> {
                    startActivity(Intent(applicationContext, AboutActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

        scribe_key.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }
    }

    private fun applyUserDarkModePreference() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)

        AppCompatDelegate.setDefaultNightMode(
            if (isUserDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isKeyboardEnabled()) {
            ConfirmationAdvancedDialog(this, messageId = R.string.redirection_note, positive = R.string.ok, negative = 0) { success ->
                if (success) {
                    Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                } else {
                    finish()
                }
            }
        }

        updateTextColors(main_holder)
        updateChangeKeyboardColor()
        main_holder.setBackgroundColor(getProperBackgroundColor())
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        updateMenuItemColors(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses = LICENSE_GSON
        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, true)
    }

    private fun updateChangeKeyboardColor() {
        val applyBackground = resources.getDrawable(R.drawable.button_background_rounded, theme) as RippleDrawable
        (applyBackground as LayerDrawable).findDrawableByLayerId(R.id.button_background_holder).applyColorFilter(getProperPrimaryColor())
        change_keyboard.background = applyBackground
        change_keyboard.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun isKeyboardEnabled(): Boolean {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledKeyboards = inputMethodManager.enabledInputMethodList
        return enabledKeyboards.any {
            it.packageName == packageName
        }
    }


    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        return
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        return
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2?.y?.minus(e1?.y ?: 0f) ?: 0f
            val diffX = e2?.x?.minus(e1?.x ?: 0f) ?: 0f
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        return false
                    } else {
                        startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }
}
