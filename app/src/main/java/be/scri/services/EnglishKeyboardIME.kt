package be.scri.services

import be.scri.R

class EnglishKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_english
}
