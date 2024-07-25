package org.scribe.services

import org.scribe.R

class PortugueseKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_portuguese
}

