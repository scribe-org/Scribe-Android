package be.scri.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_LOCALE_SETTINGS
import android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import be.scri.R
import be.scri.databinding.ActivitySettingsBinding
import be.scri.extensions.config
import be.scri.extensions.updateTextColors
import be.scri.helpers.CustomAdapter
import be.scri.models.SwitchItem
import be.scri.models.TextItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_settings.settings_scrollview
import kotlin.math.abs


class SettingsActivity : SimpleActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    private lateinit var binding: ActivitySettingsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecycleView()
        setupRecyclerView2()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        gestureDetector = GestureDetector(this)

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        for(inputMethod in enabledInputMethods) {
            if (inputMethod.packageName == "be.scri.debug") {
                binding.btnInstall.visibility = View.INVISIBLE
                binding.selectLanguage.visibility = View.VISIBLE
            }
        }
        binding.btnInstall.setOnClickListener {
            Intent(ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }


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

    private fun setupRecycleView(){
        val recyclerView = binding.recyclerViewSettings
        recyclerView.adapter = CustomAdapter(getFirstRecyclerViewData(),this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.suppressLayout(true)
    }

    private fun getFirstRecyclerViewData():List<Any> = listOf(
        TextItem(R.string.app_settings_appSettings_appLanguage , image = R.drawable.right_arrow , action = ::selectLanguage),
        SwitchItem("Dark mode", isChecked = config.darkTheme , action = ::darkMode , action2 = ::lightMode),
        SwitchItem("Vibrate on Keypress", isChecked = config.vibrateOnKeypress , action = ::enableVibrateOnKeypress , action2 = ::disableVibrateOnKeypress),
        SwitchItem("Show a popup on keypress", isChecked = config.showPopupOnKeypress , action = ::enableShowPopupOnKeypress , action2 = ::disableShowPopupOnKeypress),
    )


    private fun selectLanguage() {
        val intent: Intent = Intent(ACTION_APP_LOCALE_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)

    }

    private fun setupRecyclerView2() {
        val recyclerView = binding.recyclerView2
        val adapter = CustomAdapter(getRecyclerViewElements(),this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.suppressLayout(true)
    }

    private fun getRecyclerViewElements(): MutableList<TextItem> {
        val languages = setupKeyboardLanguage()
        val list = mutableListOf<TextItem>()
        for (language in languages) {
            var localizeLanguage:Int = 0
            when(language) {
                "English" -> localizeLanguage = R.string._global_english
                "French" -> localizeLanguage = R.string._global_french
                "German" -> localizeLanguage = R.string._global_german
                "Russian" -> localizeLanguage = R.string._global_russian
                "Spanish" -> localizeLanguage = R.string._global_spanish
                "Italian" -> localizeLanguage = R.string._global_italian
                "Portuguese" -> localizeLanguage = R.string._global_portuguese
            }
            list.add(
                TextItem(
                    text = localizeLanguage,
                    image = R.drawable.right_arrow,
                    action = { loadLanguageSettings(language) },
                    language = language
                )
            )
        }
        return list
    }
    private fun loadLanguageSettings(language: String) {
        val intent = Intent(this, LanguageSettings::class.java)
        intent.putExtra("LANGUAGE_EXTRA", language)
        startActivity(intent)
    }


    private fun setupKeyboardLanguage(): MutableList<String> {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        val result = mutableListOf<String>()

        for (inputMethod in enabledInputMethods) {
            when (inputMethod.serviceName) {
                "be.scri.services.EnglishKeyboardIME" -> result.add("English")
                "be.scri.services.GermanKeyboardIME" -> result.add("German")
                "be.scri.services.RussianKeyboardIME" -> result.add("Russian")
                "be.scri.services.SpanishKeyboardIME" -> result.add("Spanish")
                "be.scri.services.FrenchKeyboardIME" -> result.add("French")
                "be.scri.services.ItalianKeyboardIME" -> result.add("Italian")
                "be.scri.services.PortugueseKeyboardIME" -> result.add("Portuguese")
            }
        }
        return result
    }


    private fun lightMode() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("dark_mode", false)
        editor.apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


    }
    private fun darkMode(){
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("dark_mode", true)
        editor.apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private fun enableVibrateOnKeypress() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("vibrate_on_keypress", true)
        editor.apply()
        config.vibrateOnKeypress = true
    }

    private fun disableVibrateOnKeypress() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("vibrate_on_keypress", false)
        editor.apply()
        config.vibrateOnKeypress = false
    }

    private fun enableShowPopupOnKeypress() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("show_popup_on_keypress", true)
        editor.apply()
        config.showPopupOnKeypress = true
    }

    private fun disableShowPopupOnKeypress() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("show_popup_on_keypress", false)
        editor.apply()
        config.showPopupOnKeypress = false
    }

    private fun enablePeriodOnSpaceBarDoubleTap() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap", true)
        editor.apply()
        config.periodOnDoubleTap = true
    }

    private fun disablePeriodOnSpaceBarDoubleTap() {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap", false)
        editor.apply()
        config.periodOnDoubleTap = false
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(settings_scrollview)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
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
