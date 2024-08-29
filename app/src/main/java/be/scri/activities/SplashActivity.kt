package be.scri.activities

import android.content.Intent
import be.scri.fragments.MainFragment

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        startActivity(Intent(this, MainFragment::class.java))
        finish()
    }
}
