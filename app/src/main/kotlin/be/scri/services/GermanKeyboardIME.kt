package be.scri.services

import be.scri.R

class GermanKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_german
}
