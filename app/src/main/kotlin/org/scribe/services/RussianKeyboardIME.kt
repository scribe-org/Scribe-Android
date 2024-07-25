package org.scribe.services

import org.scribe.R

class RussianKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_russian
}
