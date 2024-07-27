package be.scri.services

import be.scri.R

class RussianKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_russian
}
