package be.scri.extensions

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import be.scri.R
import be.scri.databases.ClipsDatabase
import be.scri.helpers.Config
import be.scri.interfaces.ClipsDao

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.clipsDB: ClipsDao get() = ClipsDatabase.getInstance(applicationContext).ClipsDao()

fun Context.getCurrentClip(): String? {
    val clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
    return clipboardManager.primaryClip?.getItemAt(0)?.text?.trim()?.toString()
}

fun Context.getStrokeColor(): Int {
    return if (config.isUsingSystemTheme) {
        if (isUsingSystemDarkTheme()) {
            resources.getColor(R.color.md_grey_800, theme)
        } else {
            resources.getColor(R.color.md_grey_400, theme)
        }
    } else {
        val lighterColor = getProperBackgroundColor().lightenColor()
        if (lighterColor == Color.WHITE || lighterColor == Color.BLACK) {
            resources.getColor(R.color.divider_grey, theme)
        } else {
            lighterColor
        }
    }
}
