/**
 * Provides items to the navigation bar.
 *
 * Copyright (C) 2024 Scribe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
