package be.scri.services

import android.content.Context
import be.scri.R

class SwedishKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_spanish

    private fun shouldCommitPeriodAfterSpace(language: String): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("period_on_double_tap_$language", false)
    }

    override fun commitPeriodAfterSpace() {
        if (shouldCommitPeriodAfterSpace("Swedish")) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
        }
    }
}
