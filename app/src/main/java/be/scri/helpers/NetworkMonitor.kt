// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility singleton to check network connectivity status.
 */
object NetworkMonitor {
    /**
     * Checks if the device is currently connected to the internet.
     *
     * @param context The application context.
     * @return true if connected via WiFi, Cellular, or Ethernet, false otherwise.
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
