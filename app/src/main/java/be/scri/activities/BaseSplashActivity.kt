package be.scri.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import be.scri.R
import be.scri.extensions.baseConfig
import be.scri.extensions.checkAppIconColor
import be.scri.extensions.getSharedTheme
import be.scri.extensions.isThankYouInstalled
import be.scri.extensions.isUsingSystemDarkTheme

abstract class BaseSplashActivity : AppCompatActivity() {
    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        baseConfig.apply {
            if (isUsingAutoTheme) {
                val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
                isUsingSharedTheme = false
                textColor = resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color)
                backgroundColor = resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color)
                navigationBarColor = if (isUsingSystemDarkTheme) Color.BLACK else -2
            }
        }

        if (!baseConfig.isUsingAutoTheme && !baseConfig.isUsingSystemTheme && isThankYouInstalled()) {
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = true
                        isUsingSharedTheme = true
                        wasSharedThemeEverActivated = true

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        navigationBarColor = it.navigationBarColor
                        accentColor = it.accentColor
                    }

                    if (baseConfig.appIconColor != it.appIconColor) {
                        baseConfig.appIconColor = it.appIconColor
                        checkAppIconColor()
                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }
}
