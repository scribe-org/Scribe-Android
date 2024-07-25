package org.scribe.services

import org.scribe.R

class GermanKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_german
}
