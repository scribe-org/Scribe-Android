package be.scri.helpers

import android.net.Uri

class MyContentProvider {
    companion object {
        private const val AUTHORITY = "be.scri.commons.provider"
        const val SHARED_THEME_ACTIVATED = "be.scri.commons.SHARED_THEME_ACTIVATED"
        const val SHARED_THEME_UPDATED = "be.scri.commons.SHARED_THEME_UPDATED"
        val MY_CONTENT_URI = Uri.parse("content://$AUTHORITY/themes")

        const val COL_ID = "_id" // used in Simple Thank You
        const val COL_TEXT_COLOR = "text_color"
        const val COL_BACKGROUND_COLOR = "background_color"
        const val COL_PRIMARY_COLOR = "primary_color"
        const val COL_ACCENT_COLOR = "accent_color"
        const val COL_APP_ICON_COLOR = "app_icon_color"
        const val COL_NAVIGATION_BAR_COLOR = "navigation_bar_color"
        const val COL_LAST_UPDATED_TS = "last_updated_ts"

    }
}
