// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import be.scri.R
import be.scri.helpers.PreferencesHelper.getIsDarkModeOrNot

/**
 * Manages UI theme color resolutions, gradient/ripple drawables creation,
 * navigation bar colors, and system bar insets for the Scribe keyboard.
 */
class KeyboardThemeManager {
    /**
     * Calculates whether a given ARGB color is considered light based on relative luminance.
     */
    fun isLightColor(color: Int): Boolean {
        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF
        val darkness = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        return darkness < 0.5
    }

    /**
     * Applies navigation bar color, window decor insets, light/dark appearance flags, and system bar behaviors.
     */
    fun applyNavBarColor(
        service: InputMethodService,
        window: Window?,
        isFloatingMode: Boolean,
        uiManager: KeyboardUIManager?,
    ) {
        val targetWindow = window ?: return
        targetWindow.decorView.post {
            val context = service.applicationContext
            val isDarkMode = getIsDarkModeOrNot(context)
            val colorRes = if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color
            val color = ContextCompat.getColor(service, colorRes)

            WindowCompat.setDecorFitsSystemWindows(targetWindow, false)
            if (Build.VERSION.SDK_INT < 35) {
                @Suppress("DEPRECATION")
                targetWindow.navigationBarColor = Color.TRANSPARENT
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                targetWindow.isNavigationBarContrastEnforced = false
            }

            if (isFloatingMode) {
                targetWindow.decorView.setBackgroundColor(Color.TRANSPARENT)
            } else {
                targetWindow.decorView.setBackgroundColor(color)
            }
            val insetsController = WindowCompat.getInsetsController(targetWindow, targetWindow.decorView)
            insetsController.isAppearanceLightNavigationBars = isLightColor(color)

            if (isFloatingMode) {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                @Suppress("DEPRECATION")
                targetWindow.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            } else {
                insetsController.show(WindowInsetsCompat.Type.navigationBars())
                @Suppress("DEPRECATION")
                targetWindow.decorView.systemUiVisibility = 0
            }

            if (uiManager != null) {
                if (isFloatingMode) {
                    uiManager.binding.root.setBackgroundColor(Color.TRANSPARENT)
                    val kbBgColor = ContextCompat.getColor(service, if (isDarkMode) R.color.dark_keyboard_bg_color else R.color.light_keyboard_bg_color)
                    uiManager.binding.floatingDragBar.setBackgroundColor(kbBgColor)
                    val pillColor = if (isDarkMode) 0x4DFFFFFF.toInt() else 0x40000000.toInt()
                    uiManager.binding.floatingDragHandle.setColorFilter(pillColor)
                } else {
                    uiManager.binding.root.setBackgroundColor(color)
                }

                ViewCompat.setOnApplyWindowInsetsListener(uiManager.binding.root) { view, insets ->
                    val insetTypes = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    val navBarHeight = insets.getInsets(insetTypes).bottom
                    val paddingBottom = if (isFloatingMode) 0 else navBarHeight
                    view.setPadding(0, 0, 0, paddingBottom)
                    insets
                }

                uiManager.binding.root.post {
                    ViewCompat.requestApplyInsets(uiManager.binding.root)
                }
            }
        }
    }

    /**
     * Applies text colors, icon tints, ripple backgrounds, and shadow colors to empty state banner views.
     */
    fun applyBannerTheme(
        context: Context,
        banner: TextView,
        bannerContainer: View,
        isDarkMode: Boolean = getIsDarkModeOrNot(context),
        density: Float = context.resources.displayMetrics.density,
    ) {
        val bannerColor = if (isDarkMode) R.color.dark_tutorial_button_color else R.color.light_tutorial_button_color
        val bannerTextColor = if (isDarkMode) R.color.dark_button_outline_color else R.color.light_text_color
        banner.setTextColor(ContextCompat.getColor(context, bannerTextColor))

        banner.post {
            val iconColor = ContextCompat.getColor(context, bannerTextColor)
            banner.compoundDrawables.forEach { drawable ->
                drawable?.setTint(iconColor)
            }
        }

        val border = GradientDrawable()
        border.cornerRadius = 12f * density
        border.setColor(ContextCompat.getColor(context, bannerColor))

        if (isDarkMode) {
            border.setStroke(
                (1.5f * density).toInt(),
                ContextCompat.getColor(context, bannerTextColor),
            )
        }

        val rippleColor =
            ColorUtils.setAlphaComponent(
                ContextCompat.getColor(context, bannerTextColor),
                51,
            )
        val rippleDrawable = RippleDrawable(ColorStateList.valueOf(rippleColor), border, null)

        bannerContainer.background = rippleDrawable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            bannerContainer.outlineAmbientShadowColor = Color.TRANSPARENT
            bannerContainer.outlineSpotShadowColor = Color.TRANSPARENT
        }
    }

    /**
     * Applies a specific style to a suggestion button, including text, color, and a custom background.
     */
    fun applyInformativeSuggestionStyle(
        context: Context,
        button: Button,
        colorRes: Int,
        text: String,
        backgroundRes: Int,
    ) {
        button.text = text
        button.setTextColor(ContextCompat.getColor(context, R.color.white))
        button.isClickable = false
        button.setOnClickListener(null)

        val background = ContextCompat.getDrawable(context, backgroundRes)?.mutate()

        if (background is RippleDrawable) {
            val contentDrawable = background.getDrawable(0)

            if (contentDrawable is LayerDrawable) {
                val shapeDrawable =
                    contentDrawable.findDrawableByLayerId(
                        R.id.button_background_shape,
                    ) as? GradientDrawable

                shapeDrawable?.setColor(
                    ContextCompat.getColor(
                        context,
                        colorRes,
                    ),
                )
            }
        }
        button.background = background
    }

    /**
     * Applies rounded background, tint, and text color to a single suggestion button based on color resource and dark mode.
     */
    fun applySingleSuggestionStyle(
        context: Context,
        button: Button,
        colorRes: Int,
        buttonText: String,
        textSizeSp: Float? = null,
    ) {
        button.visibility = View.VISIBLE
        button.text = buttonText
        if (textSizeSp != null) {
            button.textSize = textSizeSp
        }
        button.isClickable = false
        button.setOnClickListener(null)

        if (colorRes != R.color.transparent) {
            button.background = ContextCompat.getDrawable(context, R.drawable.button_background_rounded)
            button.backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            button.background = null
            val isUserDarkMode = getIsDarkModeOrNot(context)
            button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.transparent)
            button.setTextColor(ContextCompat.getColor(context, if (isUserDarkMode) R.color.white else android.R.color.black))
        }
    }

    /**
     * Resolves text color for standard word autocompletion suggestion buttons based on dark/light mode preference.
     */
    fun getSuggestionTextColor(context: Context): Int {
        val isDarkMode = getIsDarkModeOrNot(context)
        return if (isDarkMode) Color.WHITE else "#1E1E1E".toColorInt()
    }
}
