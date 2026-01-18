// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Implements the main activity with a custom action bar, ViewPager navigation, and dynamic UI adjustments.
 */

package be.scri.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import be.scri.ui.screens.SampleScreen

/**
 * The main entry point of the app.
 * Initializes theme settings, navigation, and sets up the main UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Initializes the app on launch. Sets the theme based on user preferences, sets up edge-to-edge
     * layout, and builds the UI using Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SampleScreen()
        }
    }
}
