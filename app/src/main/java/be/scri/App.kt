package be.scri

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import be.scri.extensions.checkUseEnglish
import be.scri.extensions.config

class App : Application() {
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(
            if (config.darkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate()
        checkUseEnglish()
    }
}
