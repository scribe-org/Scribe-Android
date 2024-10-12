package be.scri.helpers

import android.content.Context
import be.scri.R
import be.scri.extensions.getInternalStoragePath
import be.scri.extensions.getSDCardPath
import be.scri.extensions.getSharedPrefs

open class BaseConfig(
    val context: Context,
) {
    protected val prefs = context.getSharedPrefs()

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }

    var primaryAndroidDataTreeUri: String
        get() = prefs.getString(PRIMARY_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(PRIMARY_ANDROID_DATA_TREE_URI, uri).apply()

    var sdAndroidDataTreeUri: String
        get() = prefs.getString(SD_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(SD_ANDROID_DATA_TREE_URI, uri).apply()

    var otgAndroidDataTreeUri: String
        get() = prefs.getString(OTG_ANDROID_DATA_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(OTG_ANDROID_DATA_TREE_URI, uri).apply()

    var primaryAndroidObbTreeUri: String
        get() = prefs.getString(PRIMARY_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(PRIMARY_ANDROID_OBB_TREE_URI, uri).apply()

    var sdAndroidObbTreeUri: String
        get() = prefs.getString(SD_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(SD_ANDROID_OBB_TREE_URI, uri).apply()

    var otgAndroidObbTreeUri: String
        get() = prefs.getString(OTG_ANDROID_OBB_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(OTG_ANDROID_OBB_TREE_URI, uri).apply()

    var sdTreeUri: String
        get() = prefs.getString(SD_TREE_URI, "")!!
        set(uri) = prefs.edit().putString(SD_TREE_URI, uri).apply()

    var otgTreeUri: String
        get() = prefs.getString(OTG_TREE_URI, "")!!
        set(OTGTreeUri) = prefs.edit().putString(OTG_TREE_URI, OTGTreeUri).apply()

    var otgPartition: String
        get() = prefs.getString(OTG_PARTITION, "")!!
        set(OTGPartition) = prefs.edit().putString(OTG_PARTITION, OTGPartition).apply()

    var otgPath: String
        get() = prefs.getString(OTG_REAL_PATH, "")!!
        set(OTGPath) = prefs.edit().putString(OTG_REAL_PATH, OTGPath).apply()

    var sdCardPath: String
        get() = prefs.getString(SD_CARD_PATH, getDefaultSDCardPath())!!
        set(sdCardPath) = prefs.edit().putString(SD_CARD_PATH, sdCardPath).apply()

    private fun getDefaultSDCardPath() = if (prefs.contains(SD_CARD_PATH)) "" else context.getSDCardPath()

    var internalStoragePath: String
        get() = prefs.getString(INTERNAL_STORAGE_PATH, getDefaultInternalPath())!!
        set(internalStoragePath) = prefs.edit().putString(INTERNAL_STORAGE_PATH, internalStoragePath).apply()

    private fun getDefaultInternalPath() = if (prefs.contains(INTERNAL_STORAGE_PATH)) "" else context.getInternalStoragePath()

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, context.resources.getColor(R.color.default_text_color))
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var keyColor: Int
        get() = prefs.getInt(KEY_COLOR, context.resources.getColor(R.color.default_key_color))
        set(keyColor) = prefs.edit().putInt(KEY_COLOR, keyColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, context.resources.getColor(R.color.default_background_color))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, context.resources.getColor(R.color.color_primary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    var accentColor: Int
        get() = prefs.getInt(ACCENT_COLOR, context.resources.getColor(R.color.color_primary))
        set(accentColor) = prefs.edit().putInt(ACCENT_COLOR, accentColor).apply()

    var navigationBarColor: Int
        get() = prefs.getInt(NAVIGATION_BAR_COLOR, INVALID_NAVIGATION_BAR_COLOR)
        set(navigationBarColor) = prefs.edit().putInt(NAVIGATION_BAR_COLOR, navigationBarColor).apply()

    var appIconColor: Int
        get() = prefs.getInt(APP_ICON_COLOR, context.resources.getColor(R.color.color_primary))
        set(appIconColor) {
            isUsingModifiedAppIcon = appIconColor != context.resources.getColor(R.color.color_primary)
            prefs.edit().putInt(APP_ICON_COLOR, appIconColor).apply()
        }

    var lastIconColor: Int
        get() = prefs.getInt(LAST_ICON_COLOR, context.resources.getColor(R.color.color_primary))
        set(lastIconColor) = prefs.edit().putInt(LAST_ICON_COLOR, lastIconColor).apply()

    var useEnglish: Boolean
        get() = prefs.getBoolean(USE_ENGLISH, false)
        set(useEnglish) {
            wasUseEnglishToggled = true
            prefs.edit().putBoolean(USE_ENGLISH, useEnglish).commit()
        }

    var wasUseEnglishToggled: Boolean
        get() = prefs.getBoolean(WAS_USE_ENGLISH_TOGGLED, false)
        set(wasUseEnglishToggled) = prefs.edit().putBoolean(WAS_USE_ENGLISH_TOGGLED, wasUseEnglishToggled).apply()

    var wasSharedThemeEverActivated: Boolean
        get() = prefs.getBoolean(WAS_SHARED_THEME_EVER_ACTIVATED, false)
        set(wasSharedThemeEverActivated) = prefs.edit().putBoolean(WAS_SHARED_THEME_EVER_ACTIVATED, wasSharedThemeEverActivated).apply()

    var isUsingSharedTheme: Boolean
        get() = prefs.getBoolean(IS_USING_SHARED_THEME, false)
        set(isUsingSharedTheme) = prefs.edit().putBoolean(IS_USING_SHARED_THEME, isUsingSharedTheme).apply()

    var isUsingAutoTheme: Boolean
        get() = prefs.getBoolean(IS_USING_AUTO_THEME, false)
        set(isUsingAutoTheme) = prefs.edit().putBoolean(IS_USING_AUTO_THEME, isUsingAutoTheme).apply()

    var isUsingSystemTheme: Boolean
        get() = prefs.getBoolean(IS_USING_SYSTEM_THEME, false)
        set(isUsingSystemTheme) = prefs.edit().putBoolean(IS_USING_SYSTEM_THEME, isUsingSystemTheme).apply()

    var wasSharedThemeForced: Boolean
        get() = prefs.getBoolean(WAS_SHARED_THEME_FORCED, false)
        set(wasSharedThemeForced) = prefs.edit().putBoolean(WAS_SHARED_THEME_FORCED, wasSharedThemeForced).apply()

    var isUsingModifiedAppIcon: Boolean
        get() = prefs.getBoolean(IS_USING_MODIFIED_APP_ICON, false)
        set(isUsingModifiedAppIcon) = prefs.edit().putBoolean(IS_USING_MODIFIED_APP_ICON, isUsingModifiedAppIcon).apply()

    var appId: String
        get() = prefs.getString(APP_ID, "")!!
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var appSideloadingStatus: Int
        get() = prefs.getInt(APP_SIDELOADING_STATUS, SIDELOADING_UNCHECKED)
        set(appSideloadingStatus) = prefs.edit().putInt(APP_SIDELOADING_STATUS, appSideloadingStatus).apply()

}
