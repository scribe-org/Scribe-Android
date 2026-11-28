// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.dataupdate

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

/**
 * One request to open the download screen, including a fresh id so a repeated tap navigates again.
 */
data class DownloadNavRequest(
    val route: String,
    val id: Long,
)

/**
 * Holds the latest `navigate_to` extra from a launch intent or a notification tap.
 */
class DownloadNavEvents {
    var request by mutableStateOf<DownloadNavRequest?>(null)
        private set

    /**
     * Records [intent]'s navigate extra. An intent without the extra leaves the previous request alone.
     */
    fun publish(intent: Intent) {
        val route = intent.getStringExtra(DataUpdateNotifier.EXTRA_NAVIGATE_TO) ?: return
        request = DownloadNavRequest(route, System.nanoTime())
    }
}

/**
 * Navigates to [route] when a matching request arrives, including one that was published before composition.
 */
@Composable
fun DownloadNavEffect(
    events: DownloadNavEvents,
    navController: NavHostController,
    route: String,
) {
    val request = events.request
    LaunchedEffect(request?.id) {
        if (request?.route == route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }
}
