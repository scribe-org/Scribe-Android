package be.scri.activities

import android.app.UiModeManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import be.scri.R
import be.scri.adapters.ViewPagerAdapter
import be.scri.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.custom_action_bar_layout)
        supportActionBar?.elevation = 0F
        val layoutParams = supportActionBar?.customView?.layoutParams
        layoutParams?.height = 1000
        supportActionBar?.customView?.layoutParams = layoutParams
        setActionBarTitle(R.string.app_launcher_name)
        val mButton = supportActionBar?.customView?.findViewById<Button>(R.id.button)
        val mImage = getDrawable(R.drawable.chevron)
        applyUserDarkModePreference(this)
        mButton?.setCompoundDrawablesWithIntrinsicBounds(mImage, null, null, null)
        mButton?.compoundDrawablePadding = 2
        mButton?.visibility = View.GONE

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        if (savedInstanceState == null) {
            viewPager.setCurrentItem(0, false)
        }

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    bottomNavigationView.menu.getItem(position).isChecked = true
                    when (position) {
                        0 -> {
                            binding.fragmentContainer.visibility = View.GONE
                            setActionBarTitle(R.string.app_launcher_name)
                            setActionBarButtonInvisible()
                            unsetActionBarLayoutMargin()
                        }

                        1 -> {
                            binding.fragmentContainer.visibility = View.GONE
                            setActionBarTitle(R.string.app_settings_title)
                            setActionBarButtonInvisible()
                            unsetActionBarLayoutMargin()
                        }

                        2 -> {
                            binding.fragmentContainer.visibility = View.GONE
                            setActionBarButtonInvisible()
                            setActionBarTitle(R.string.app_about_title)
                            unsetActionBarLayoutMargin()
                        }

                        else -> {
                            binding.fragmentContainer.visibility = View.VISIBLE
                        }
                    }
                }
            },
        )

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.installation -> {
                    viewPager.setCurrentItem(0, true)
                    binding.fragmentContainer.visibility = View.GONE
                    setActionBarTitle(R.string.app_launcher_name)

                    true
                }

                R.id.info -> {
                    viewPager.setCurrentItem(2, true)
                    binding.fragmentContainer.visibility = View.GONE
                    setActionBarTitle(R.string.app_about_title)
                    true
                }

                R.id.settings -> {
                    viewPager.setCurrentItem(1, true)
                    binding.fragmentContainer.visibility = View.GONE
                    setActionBarTitle(R.string.app_settings_title)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    fun showFragmentContainer() {
        binding.fragmentContainer.visibility = View.VISIBLE
    }

    fun setActionBarTitle(title: Int) {
        supportActionBar?.customView?.findViewById<TextView>(R.id.name)?.text = getString(title)
    }

    fun setActionBarButtonVisible() {
        supportActionBar?.customView?.findViewById<Button>(R.id.button)?.visibility = View.VISIBLE
    }

    fun setActionBarButtonInvisible() {
        supportActionBar?.customView?.findViewById<Button>(R.id.button)?.visibility = View.GONE
    }

    fun setActionBarButtonFunction(
        page: Int,
        title: Int,
    ) {
        val button = supportActionBar?.customView?.findViewById<Button>(R.id.button)
        button?.setOnClickListener {
            val viewpager = findViewById<ViewPager2>(R.id.view_pager)
            val frameLayout = findViewById<ViewGroup>(R.id.fragment_container)
            if (viewpager.currentItem == page) {
                frameLayout.visibility = View.GONE
                viewpager.setCurrentItem(page, true)
            }
            frameLayout.visibility = View.GONE
            unsetActionBarLayoutMargin()
            setActionBarTitle(title)
            button.visibility = View.GONE
        }
    }

    fun setActionBarLayoutMargin() {
        val textView = supportActionBar?.customView?.findViewById<TextView>(R.id.name)
        val params = textView?.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = -50
        params.bottomMargin = 30
        textView.layoutParams = params
    }

    fun unsetActionBarLayoutMargin() {
        val textView = supportActionBar?.customView?.findViewById<TextView>(R.id.name)
        val params = textView?.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 50
        params.bottomMargin = 0
        textView.layoutParams = params
    }

    private fun applyUserDarkModePreference(context: Context) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isSystemDarkTheme = isDarkMode(context)
        val isUserDarkMode = sharedPref.getBoolean("dark_mode", isSystemDarkTheme)
        AppCompatDelegate.setDefaultNightMode(
            if (isUserDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            },
        )
    }

    fun isDarkMode(context: Context): Boolean {
        val uiModeManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
    }

    fun showHint(
        sharedPrefsKey: String,
        hintMessageResId: Int,
    ) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val allEntries = sharedPref.all
        for ((key, value) in allEntries) {
            Log.i("hint", "$key = $value")
        }
        val isHintShown = sharedPref.getBoolean(sharedPrefsKey, false)
        Log.i("hint", isHintShown.toString())
        if (!isHintShown) {
            val hintLayout = findViewById<View>(R.id.hint_layout)
            val hintText = findViewById<TextView>(R.id.hint_text)
            hintText.text = getString(hintMessageResId)

            hintLayout.visibility = View.VISIBLE

            val okButton = findViewById<Button>(R.id.hint_ok_button)
            okButton.setOnClickListener {
                with(sharedPref.edit()) {
                    putBoolean(sharedPrefsKey, true)
                    apply()
                }
                hideHint()
            }
        }
    }

    fun hideHint() {
        val hintLayout = findViewById<View>(R.id.hint_layout)
        hintLayout.visibility = View.GONE
    }
}
