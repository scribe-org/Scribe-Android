package be.scri.services

import be.scri.R

class PortugueseKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_portuguese
}

