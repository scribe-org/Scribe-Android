package org.scribe.services

import org.scribe.R

class ItalianKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_italian
}

