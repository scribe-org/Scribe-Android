package be.scri.services

import android.content.Context
import android.util.Log
import be.scri.R

class PortugueseKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_portuguese
    private var lastSpaceTime: Long = 0
    private val doubleTapThreshold: Long = 300

    private fun shouldCommitPeriodAfterSpace(language: String): Boolean {
        val sharedPref = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("period_on_double_tap_$language", false)
    }

    override fun commitPeriodAfterSpace() {
        if (shouldCommitPeriodAfterSpace("Portuguese")) {
            val inputConnection = currentInputConnection ?: return
            inputConnection.deleteSurroundingText(1, 0)
            inputConnection.commitText(". ", 1)
        }
    }
}

