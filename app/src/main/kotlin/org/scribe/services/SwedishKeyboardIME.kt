package org.scribe.services

import org.scribe.R

class SwedishKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_spanish
}
