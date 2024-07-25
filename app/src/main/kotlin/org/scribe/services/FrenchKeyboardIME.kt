package org.scribe.services

import org.scribe.R

class FrenchKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_french
}

