package be.scri.services

import be.scri.R

class ItalianKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_italian
}

