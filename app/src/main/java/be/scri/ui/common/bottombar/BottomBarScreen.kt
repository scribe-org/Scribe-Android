// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Provides items to the navigation bar.
 */

package be.scri.ui.common.bottombar

import androidx.annotation.DrawableRes
import be.scri.R
import be.scri.navigation.Screen

sealed class BottomBarScreen(
    val route: String,
    @DrawableRes val icon: Int,
    val label: String,
) {
    data object Installation : BottomBarScreen(
        Screen.Installation.route,
        R.drawable.material_keyboard,
        "Installation",
    )

    data object Settings : BottomBarScreen(
        Screen.Settings.route,
        R.drawable.material_settings,
        "Settings",
    )

    data object About : BottomBarScreen(
        Screen.About.route,
        R.drawable.material_info,
        "About",
    )
}

val bottomBarScreens =
    listOf(
        BottomBarScreen.Installation,
        BottomBarScreen.Settings,
        BottomBarScreen.About,
    )
