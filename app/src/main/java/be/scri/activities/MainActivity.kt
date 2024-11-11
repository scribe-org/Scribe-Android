package be.scri.activities

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
import be.scri.helpers.PreferencesHelper
import be.scri.services.EnglishKeyboardIME
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var binding: ActivityMainBinding
    private var englishKeyboardIME: EnglishKeyboardIME? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.custom_action_bar_layout)
        supportActionBar?.elevation = 0F
        val layoutParams = supportActionBar?.customView?.layoutParams
        layoutParams?.height = DEFAULT_HEIGHT
        supportActionBar?.customView?.layoutParams = layoutParams
        setActionBarTitle(R.string.app_launcher_name)
        val mButton = supportActionBar?.customView?.findViewById<Button>(R.id.button)
        val mImage = getDrawable(R.drawable.chevron)
        AppCompatDelegate.setDefaultNightMode(PreferencesHelper.getUserDarkModePreference(this))
        mButton?.setCompoundDrawablesWithIntrinsicBounds(mImage, null, null, null)
        mButton?.compoundDrawablePadding = 2
        mButton?.visibility = View.GONE

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        englishKeyboardIME = EnglishKeyboardIME()

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
                            setActionBarButtonVisibility(false)
                            setActionBarVisibility(false)
                        }

                        1 -> {
                            binding.fragmentContainer.visibility = View.GONE
                            setActionBarTitle(R.string.app_settings_title)
                            setActionBarButtonVisibility(false)
                            setActionBarVisibility(false)
                        }

                        2 -> {
                            binding.fragmentContainer.visibility = View.GONE
                            setActionBarButtonVisibility(false)
                            setActionBarTitle(R.string.app_about_title)
                            setActionBarVisibility(false)
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

    fun setActionBarButtonVisibility(visible: Boolean) {
        if (visible) {
            supportActionBar?.customView?.findViewById<Button>(R.id.button)?.visibility = View.VISIBLE
        } else {
            supportActionBar?.customView?.findViewById<Button>(R.id.button)?.visibility = View.GONE
        }
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
            setActionBarVisibility(false)
            setActionBarTitle(title)
            button.visibility = View.GONE
        }
    }

    fun setActionBarVisibility(shouldShowOnScreen: Boolean) {
        val textView = supportActionBar?.customView?.findViewById<TextView>(R.id.name) ?: return
        val params = textView.layoutParams as ViewGroup.MarginLayoutParams
        if (shouldShowOnScreen) {
            params.topMargin = ACTION_BAR_TOP_MARGIN_VISIBLE
            params.bottomMargin = ACTION_BAR_BOTTOM_MARGIN_VISIBLE
        } else {
            params.topMargin = ACTION_BAR_TOP_MARGIN_HIDDEN
            params.bottomMargin = ACTION_BAR_BOTTOM_MARGIN_HIDDEN
        }
        textView.layoutParams = params
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

    override fun onBackPressed() {
        super.onBackPressed()
        if (viewPager.currentItem == 0) {
            if (binding.fragmentContainer.visibility == View.VISIBLE) {
                binding.fragmentContainer.visibility = View.GONE
            } else {
                finish()
            }
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    fun hideHint() {
        val hintLayout = findViewById<View>(R.id.hint_layout)
        hintLayout.visibility = View.GONE
    }

    companion object {
        private const val DEFAULT_HEIGHT = 1000
        private const val ACTION_BAR_TOP_MARGIN_VISIBLE = -50
        private const val ACTION_BAR_TOP_MARGIN_HIDDEN = 50
        private const val ACTION_BAR_BOTTOM_MARGIN_VISIBLE = 30
        private const val ACTION_BAR_BOTTOM_MARGIN_HIDDEN = 0
    }
}
