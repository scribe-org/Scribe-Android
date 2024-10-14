package be.scri.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import be.scri.R
import be.scri.databinding.DialogTitleBinding
import be.scri.dialogs.AppSideloadedDialog
import be.scri.helpers.SIDELOADING_FALSE
import be.scri.helpers.SIDELOADING_TRUE
import be.scri.helpers.ensureBackgroundThread
import be.scri.helpers.isOnMainThread
import be.scri.views.MyTextView

private lateinit var binding: DialogTitleBinding

fun AppCompatActivity.updateActionBarTitle(
    text: String,
    color: Int = getProperStatusBarColor(),
) {
    val colorToUse =
        if (baseConfig.isUsingSystemTheme) {
            getColorWithDefault(R.color.you_neutral_text_color, baseConfig.textColor)
        } else {
            color.getContrastColor()
        }

    supportActionBar?.title = Html.fromHtml("<font color='${colorToUse.toHex()}'>$text</font>")
}

fun Activity.launchViewIntent(url: String) {
    ensureBackgroundThread {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            launchActivityIntent(this)
        }
    }
}

fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync()
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun Activity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog,
    titleId: Int = 0,
    titleText: String = "",
    cancelOnTouchOutside: Boolean = true,
    callback: (() -> Unit)? = null,
) {
    if (isDestroyed || isFinishing) {
        return
    }
    binding = DialogTitleBinding.inflate(layoutInflater)
    val textColor = getColorWithDefault(R.color.you_neutral_text_color, baseConfig.textColor)
    val primaryColor = getProperPrimaryColor()
    if (view is ViewGroup) {
        updateTextColors(view)
    } else if (view is MyTextView) {
        view.setColors(textColor, primaryColor)
    }

    var title: TextView? = null
    if (titleId != 0 || titleText.isNotEmpty()) {
        title = layoutInflater.inflate(R.layout.dialog_title, null) as TextView
        binding.apply {
            dialogTitleTextview.apply {
                if (titleText.isNotEmpty()) {
                    text = titleText
                } else {
                    setText(titleId)
                }
                setTextColor(textColor)
            }
        }
    }

    // if we use the same primary and background color, use the text color for dialog confirmation buttons
    val dialogButtonColor =
        if (primaryColor == baseConfig.backgroundColor) {
            textColor
        } else {
            primaryColor
        }

    dialog.apply {
        setView(view)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCustomTitle(title)
        setCanceledOnTouchOutside(cancelOnTouchOutside)
        show()
        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
        getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialogButtonColor)

        val bgDrawable =
            when {
                isBlackAndWhiteTheme() -> resources.getDrawable(R.drawable.black_dialog_background, theme)
                baseConfig.isUsingSystemTheme -> resources.getDrawable(R.drawable.dialog_you_background, theme)
                else -> resources.getColoredDrawableWithColor(R.drawable.dialog_bg, baseConfig.backgroundColor)
            }

        window?.setBackgroundDrawable(bgDrawable)
    }
    callback?.invoke()
}

fun Activity.checkAppSideloading(): Boolean {
    val isSideloaded =
        when (baseConfig.appSideloadingStatus) {
            SIDELOADING_TRUE -> true
            SIDELOADING_FALSE -> false
            else -> isAppSideloaded()
        }

    baseConfig.appSideloadingStatus = if (isSideloaded) SIDELOADING_TRUE else SIDELOADING_FALSE
    if (isSideloaded) {
        showSideloadingDialog()
    }

    return isSideloaded
}

fun Activity.isAppSideloaded(): Boolean =
    try {
        getDrawable(R.drawable.ic_camera_vector)
        false
    } catch (e: Exception) {
        true
    }

fun Activity.showSideloadingDialog() {
    AppSideloadedDialog(this) {
        finish()
    }
}
