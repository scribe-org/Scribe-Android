package org.scribe.extensions

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import org.scribe.R
import org.scribe.commons.extensions.getProperBackgroundColor
import org.scribe.commons.extensions.isUsingSystemDarkTheme
import org.scribe.commons.extensions.lightenColor
import org.scribe.databases.ClipsDatabase
import org.scribe.helpers.Config
import org.scribe.interfaces.ClipsDao

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
