package be.scri.extensions

import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.BaseColumns
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.ContactsContract.CommonDataKinds.BaseTypes
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.MediaStore.Audio
import android.provider.MediaStore.MediaColumns
import android.provider.OpenableColumns
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.loader.content.CursorLoader
import be.scri.R
import be.scri.helpers.BaseConfig
import be.scri.helpers.MyContentProvider
import be.scri.helpers.PREFS_KEY
import be.scri.helpers.TIME_FORMAT_12
import be.scri.helpers.TIME_FORMAT_24
import be.scri.helpers.isOnMainThread

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

val Context.isRTLLayout: Boolean get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

fun Context.toast(
    msg: String,
    length: Int = Toast.LENGTH_SHORT,
) {
    try {
        val showToast = { doToast(this, msg, length) }
        if (isOnMainThread()) {
            showToast()
        } else {
            Handler(Looper.getMainLooper()).post(showToast)
        }
    } catch (e: IllegalArgumentException) {
        Log.e("ToastError", "Invalid argument while showing toast: ${e.message}", e)
    }
}

private fun doToast(
    context: Context,
    message: String,
    length: Int,
) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.showErrorToast(
    msg: String,
    length: Int = Toast.LENGTH_LONG,
) {
    toast(String.format(getString(R.string.an_error_occurred), msg), length)
}

fun Context.showErrorToast(
    exception: Exception,
    length: Int = Toast.LENGTH_LONG,
) {
    showErrorToast(exception.toString(), length)
}

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)
val Context.sdCardPath: String get() = baseConfig.sdCardPath
val Context.internalStoragePath: String get() = baseConfig.internalStoragePath
val Context.otgPath: String get() = baseConfig.otgPath

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit,
) {
    try {
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    callback(it)
                } while (it.moveToNext())
            } else {
                Log.w("QueryCursor", "Cursor is empty for URI: $uri")
            }
        } ?: run {
            Log.w("QueryCursor", "Cursor is null for URI: $uri")
        }
    } catch (e: SecurityException) {
        Log.e("QueryCursor", "SecurityException while querying URI: $uri", e)
        if (showErrors) {
            showErrorToast(e)
        }
    } catch (e: IllegalArgumentException) {
        Log.e("QueryCursor", "IllegalArgumentException: ${e.message}", e)
        if (showErrors) {
            showErrorToast(e)
        }
    }
}
fun Context.getMyContentProviderCursorLoader() = CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)


fun Context.isThankYouInstalled() = isPackageInstalled("com.simplemobiletools.thankyou")

fun Context.isPackageInstalled(pkgName: String): Boolean =
    try {
        packageManager.getPackageInfo(pkgName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("PackageCheck", "Package not found: $pkgName", e)
        false
    }


fun Context.getTimeFormat() = if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12

val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager
