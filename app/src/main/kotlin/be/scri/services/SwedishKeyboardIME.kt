package be.scri.services

import be.scri.R

class SwedishKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_spanish
}
