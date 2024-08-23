package be.scri.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import be.scri.R
import be.scri.databinding.ActivityLanguageSettingsBinding
import be.scri.extensions.config
import be.scri.helpers.CustomAdapter
import be.scri.models.SwitchItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class LanguageSettings : SimpleActivity() {
    private lateinit var binding: ActivityLanguageSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLanguageSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        val language = intent.getStringExtra("LANGUAGE_EXTRA")
        setContentView(view)
        if (language != null) {
            setupRecyclerView(language=language)
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.info -> {
                    startActivity(Intent(applicationContext, AboutActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.settings ->  {
                    return@OnNavigationItemSelectedListener true }
                R.id.installation -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        bottomNavigationView.selectedItemId = R.id.settings

    }
    private fun setupRecyclerView(language: String) {
        val recyclerView1 = binding.recyclerView
        recyclerView1.layoutManager = LinearLayoutManager(this)
        recyclerView1.adapter = CustomAdapter(getRecyclerViewData(language), this)
        recyclerView1.suppressLayout(true)
    }

    private fun getRecyclerViewData(language: String): List<Any> {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return listOf(
        SwitchItem(
            isChecked = sharedPref.getBoolean("period_on_double_tap_$language", false),
            title = "Double space Periods",
            action = { enablePeriodOnSpaceBarDoubleTap(language = language) },
            action2 = { disablePeriodOnSpaceBarDoubleTap(language = language) }))
    }
    private fun enablePeriodOnSpaceBarDoubleTap(language: String) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", true)
        editor.apply()
    }

    private fun disablePeriodOnSpaceBarDoubleTap(language: String) {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("period_on_double_tap_$language", false)
        editor.apply()
    }

}


