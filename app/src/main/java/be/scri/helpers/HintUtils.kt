package be.scri.helpers

import android.content.Context
import android.util.Log

class HintUtils {
    companion object {

        fun resetHints(context: Context) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            Log.d("HintUtils", "Resetting hints")
            with(sharedPref.edit()) {
                putBoolean("hint_shown_main", false)
                putBoolean("hint_shown_settings", false)
                putBoolean("hint_shown_about", false)
                apply()
            }
        }
    }
}
