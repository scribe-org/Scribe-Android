package be.scri.extensions

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import be.scri.helpers.BaseConfig
import be.scri.helpers.PREFS_KEY
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

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager
