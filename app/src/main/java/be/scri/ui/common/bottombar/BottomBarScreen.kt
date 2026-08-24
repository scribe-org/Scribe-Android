// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.common.bottombar

import androidx.annotation.DrawableRes
import be.scri.R
import be.scri.helpers.AppFlavor
import be.scri.helpers.FlavorProvider
import be.scri.navigation.Screen

/**
 * Provides items to the navigation bar.
 */
sealed class BottomBarScreen(
    val route: String,
    @DrawableRes val icon: Int,
    val labelRes: Int,
    val altTextRes: Int,
) {
    /**
     * Represents the Installation screen and its associated route.
     */
    data object Installation : BottomBarScreen(
        Screen.Installation.route,
        R.drawable.material_keyboard,
        R.string.i18n_app_installation_title,
        R.string.i18n_app_installation_title_alt_text,
    )

    /**
     * Represents the Conjugate screen and its associated route.
     */
    data object Conjugate : BottomBarScreen(
        Screen.Conjugate.route,
        R.drawable.material_keyboard,
        R.string.i18n_app_conjugate_title,
        R.string.i18n_app_conjugate_title_alt_text,
    )

    /**
     * Represents the Settings screen and its associated route.
     */
    data object Settings : BottomBarScreen(
        Screen.Settings.route,
        R.drawable.material_settings,
        R.string.i18n_app_settings_title,
        R.string.i18n_app_settings_title_alt_text,
    )

    /**
     * Represents the About screen and its associated route.
     */
    data object About : BottomBarScreen(
        Screen.About.route,
        R.drawable.material_info,
        R.string.i18n_app_about_title,
        R.string.i18n_app_about_title_alt_text,
    )

    companion object {
        /**
         * Returns the list of screens to be displayed in the bottom bar based on the app flavor.
         */
        fun getScreens(flavor: AppFlavor): List<BottomBarScreen> =
            when (flavor) {
                AppFlavor.CONJUGATE -> listOf(Conjugate, Settings, About)
                AppFlavor.KEYBOARDS -> listOf(Installation, Settings, About)
            }

        /**
         * Gets the list of screens to be displayed in the bottom bar.
         */
        fun getScreens(): List<BottomBarScreen> = getScreens(FlavorProvider.get())
    }
}
