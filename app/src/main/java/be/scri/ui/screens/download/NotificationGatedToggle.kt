// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

/**
 * Decides when the "Regularly update data" switch may actually turn on.
 * Turning it on needs the notification permission on Android 13 and newer, because a check whose
 * result can never be shown is pointless. The change is applied only once the permission is there,
 * so a denial leaves the switch off and nothing is saved or scheduled.
 * Turning it off never needs a permission.
 */
internal class NotificationGatedToggle(
    private val needsPermission: () -> Boolean,
    private val requestPermission: () -> Unit,
    private val apply: (Boolean) -> Unit,
) {
    /** The user flipped the switch to [checked]. */
    fun onUserToggle(checked: Boolean) {
        if (checked && needsPermission()) {
            requestPermission()
        } else {
            apply(checked)
        }
    }

    /** The system permission dialog was answered. A denial changes nothing. */
    fun onPermissionResult(granted: Boolean) {
        if (granted) apply(true)
    }
}
