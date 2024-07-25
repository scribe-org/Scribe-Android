package org.scribe.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_settings.*
import org.scribe.R
import org.scribe.dialogs.RadioGroupDialog
import org.scribe.extensions.config
import org.scribe.extensions.updateTextColors
import org.scribe.helpers.*
import org.scribe.models.RadioItem
import kotlin.math.abs

class SettingsActivity : SimpleActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        gestureDetector = GestureDetector(this)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNavigationView != null) {
            bottomNavigationView.selectedItemId = R.id.settings

            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.installation -> {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        overridePendingTransition(0, 0)
                        true
                    }
                    R.id.settings -> true
                    R.id.info -> {
                        startActivity(Intent(applicationContext, AboutActivity::class.java))
                        overridePendingTransition(0, 0)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupDarkTheme()
        setupPeriodOnSpaceBarDoubleTap()
        setupVibrateOnKeypress()
        setupShowPopupOnKeypress()
        setupKeyboardLanguage()
        updateTextColors(settings_scrollview)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupVibrateOnKeypress() {
        settings_vibrate_on_keypress.isChecked = config.vibrateOnKeypress
        settings_vibrate_on_keypress_holder.setOnClickListener {
            settings_vibrate_on_keypress.toggle()
            config.vibrateOnKeypress = settings_vibrate_on_keypress.isChecked
        }
    }

    private fun setupPeriodOnSpaceBarDoubleTap() {
        settings_period_on_space_bar.isChecked = config.periodOnDoubleTap
        settings_period_on_space_bar_holder.setOnClickListener {
            settings_period_on_space_bar.toggle()
            config.periodOnDoubleTap = settings_period_on_space_bar.isChecked
        }
    }

    private fun setupDarkTheme() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkMode)

        settings_dark_mode.isChecked = isUserDarkMode

        settings_dark_mode_holder.setOnClickListener {
            settings_dark_mode.toggle()
            editor.putBoolean("dark_mode", settings_dark_mode.isChecked)
            editor.apply()

            if (settings_dark_mode.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            recreate()
        }
    }

    private fun setupShowPopupOnKeypress() {
        settings_show_popup_on_keypress.isChecked = config.showPopupOnKeypress
        settings_show_popup_on_keypress_holder.setOnClickListener {
            settings_show_popup_on_keypress.toggle()
            config.showPopupOnKeypress = settings_show_popup_on_keypress.isChecked
        }
    }

    private fun setupKeyboardLanguage() {
        settings_keyboard_language.text = getKeyboardLanguageText(config.keyboardLanguage)
        settings_keyboard_language_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(
                    LANGUAGE_ENGLISH_QWERTY,
                    getKeyboardLanguageText(LANGUAGE_ENGLISH_QWERTY)
                ),
                RadioItem(LANGUAGE_FRENCH, getKeyboardLanguageText(LANGUAGE_FRENCH)),
                RadioItem(LANGUAGE_GERMAN, getKeyboardLanguageText(LANGUAGE_GERMAN)),
                RadioItem(LANGUAGE_ITALIAN, getKeyboardLanguageText(LANGUAGE_ITALIAN)),
                RadioItem(LANGUAGE_PORTUGUESE, getKeyboardLanguageText(LANGUAGE_PORTUGUESE)),
                RadioItem(LANGUAGE_RUSSIAN, getKeyboardLanguageText(LANGUAGE_RUSSIAN)),
                RadioItem(LANGUAGE_SPANISH, getKeyboardLanguageText(LANGUAGE_SPANISH)),
                RadioItem(LANGUAGE_SWEDISH, getKeyboardLanguageText(LANGUAGE_SWEDISH))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.keyboardLanguage) {
                config.keyboardLanguage = it as Int
                settings_keyboard_language.text = getKeyboardLanguageText(config.keyboardLanguage)
            }
        }
    }

    private fun getKeyboardLanguageText(language: Int): String {
        return when (language) {
            LANGUAGE_FRENCH -> getString(R.string.translation_french)
            LANGUAGE_GERMAN -> getString(R.string.translation_german)
            LANGUAGE_ITALIAN -> getString(R.string.translation_italian)
            LANGUAGE_PORTUGUESE -> getString(R.string.translation_portuguese)
            LANGUAGE_RUSSIAN -> getString(R.string.translation_russian)
            LANGUAGE_SPANISH -> getString(R.string.translation_spanish)
            LANGUAGE_SWEDISH -> getString(R.string.translation_swedish)
            else -> getString(R.string.translation_english)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        }
        else {
            super.onTouchEvent(event)
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
            val diffY = e2.y - e1!!.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }
                    else {
                        startActivity(Intent(applicationContext, AboutActivity::class.java))
                    }
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }
    }

