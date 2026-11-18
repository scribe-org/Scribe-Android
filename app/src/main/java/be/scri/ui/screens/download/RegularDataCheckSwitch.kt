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
 * Notification permission is requested on Android 13 and newer. A denial still leaves the switch on.
 */
@Composable
fun RegularDataCheckSwitch(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var enabled by remember {
        mutableStateOf(PreferencesHelper.getRegularlyCheckForDataUpdates(context))
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Log.i(TAG, "Notification permission denied. Data checks will still run.")
            }
        }

    SwitchableItemComp(
        title = stringResource(R.string.i18n_app_download_menu_ui_update_data_regular_update),
        isChecked = enabled,
        onCheckedChange = { checked ->
            enabled = checked
            if (checked && needsNotificationPermission(context)) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            DataUpdateScheduler.onToggle(context, checked)
        },
        modifier = modifier,
    )
}

private fun needsNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    return status != PackageManager.PERMISSION_GRANTED
}

private const val TAG = "RegularDataCheckSwitch"
