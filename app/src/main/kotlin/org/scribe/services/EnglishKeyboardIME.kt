package org.scribe.services

import org.scribe.R

class EnglishKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_english
}
