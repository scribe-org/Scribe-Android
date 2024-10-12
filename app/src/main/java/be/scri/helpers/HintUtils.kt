package be.scri.helpers

import android.content.Context
import android.util.Log

class HintUtils {
    companion object {
//        fun showHintIfNeeded(context: Context, rootView: ViewGroup, sharedPrefsKey: String, hintLayoutResId: Int, hintMessage: Int) {
//            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
//            sharedPref.edit().putBoolean(sharedPrefsKey, false).apply()
//            val isHintShown = sharedPref.getBoolean(sharedPrefsKey, false)
//            if (!isHintShown) {
//                val hintTag = "hint_shown_$sharedPrefsKey"
//                val existingHintView = rootView.findViewWithTag<View>(hintTag)
//                existingHintView?.let { rootView.removeView(it) }
//                val hintView = LayoutInflater.from(context).inflate(hintLayoutResId, rootView, false)
//                hintView.tag = hintTag
//                rootView.addView(hintView)
//                val hintTextView = hintView.findViewById<TextView>(R.id.hint_text)
//                hintTextView.text = context.getString(hintMessage)
//                val okButton = hintView.findViewById<Button>(R.id.hint_ok_button)
//                okButton.setOnClickListener {
//                    with(sharedPref.edit()) {
//                        putBoolean(sharedPrefsKey, true)
//                        apply()
//                    }
//                    rootView.removeView(hintView)
//                }
//            }
//        }

        fun resetHints(context: Context) {
            val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            Log.d("HintUtils", "Resetting hints")
            with(sharedPref.edit()) {
                putBoolean("hint_shown_main", false)
                putBoolean("hint_shown_settings", false)
                putBoolean("hint_shown_about", false)
                apply()
            }
            Log.d("HintUtils", "hint_shown_main: ${sharedPref.getBoolean("hint_shown_main", false)}")
            Log.d("HintUtils", "hint_shown_settings: ${sharedPref.getBoolean("hint_shown_settings", false)}")
            Log.d("HintUtils", "hint_shown_about: ${sharedPref.getBoolean("hint_shown_about", false)}")
        }
    }
}
