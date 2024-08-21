package be.scri.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import be.scri.R
import be.scri.helpers.APP_ICON_IDS
import be.scri.helpers.APP_LAUNCHER_NAME
import com.google.android.material.bottomnavigation.BottomNavigationView


class WikimediaScribeActivity : BaseSimpleActivity(){
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wikimedia_scribe)
        enableEdgeToEdge()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.info

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.info ->  {
                    return@OnNavigationItemSelectedListener true }
                R.id.installation -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        bottomNavigationView.selectedItemId = R.id.info
    }

    }



