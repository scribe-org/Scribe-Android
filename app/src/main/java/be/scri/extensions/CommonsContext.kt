package be.scri.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.loader.content.CursorLoader
import be.scri.R
import be.scri.helpers.BaseConfig
import be.scri.helpers.MyContentProvider
import be.scri.helpers.PREFS_KEY
import be.scri.helpers.isOnMainThread

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

fun Context.toast(
    id: Int,
    length: Int = Toast.LENGTH_SHORT,
) {
    toast(getString(id), length)
}

fun Context.toast(
    msg: String,
    length: Int = Toast.LENGTH_SHORT,
) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {
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

fun Context.launchActivityIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast(R.string.no_app_found)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Context.getMyContentProviderCursorLoader() = CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)

fun Context.getStringsPackageName() = getString(R.string.package_name)
