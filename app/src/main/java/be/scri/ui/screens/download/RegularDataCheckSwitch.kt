// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import be.scri.R
import be.scri.helpers.PreferencesHelper
import be.scri.helpers.dataupdate.DataUpdateScheduler
import be.scri.ui.common.components.SwitchableItemComp

/**
 * The saved "Regularly update data" switch.
 * The label stays on the existing i18n string. Turning it on checks now, then once each month.
 * If the notification permission is denied on Android 13 and newer, the switch stays off.
 */
@Composable
fun RegularDataCheckSwitch(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var enabled by remember {
        mutableStateOf(PreferencesHelper.getRegularlyCheckForDataUpdates(context))
    }
    val toggleHolder = remember { ToggleHolder() }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Log.i(TAG, "Notification permission denied. Data checks stay off.")
            }
            toggleHolder.toggle?.onPermissionResult(granted)
        }
    toggleHolder.toggle =
        remember(context, permissionLauncher) {
            NotificationGatedToggle(
                needsPermission = { needsNotificationPermission(context) },
                requestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                apply = { checked ->
                    enabled = checked
                    DataUpdateScheduler.onToggle(context, checked)
                },
            )
        }

    SwitchableItemComp(
        title = stringResource(R.string.i18n_app_download_menu_ui_update_data_regular_update),
        isChecked = enabled,
        onCheckedChange = { checked -> toggleHolder.toggle?.onUserToggle(checked) },
        modifier = modifier,
    )
}

private fun needsNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    return status != PackageManager.PERMISSION_GRANTED
}

/** Lets the permission callback reach the toggle that is created after the launcher. */
private class ToggleHolder {
    var toggle: NotificationGatedToggle? = null
}

private const val TAG = "RegularDataCheckSwitch"
