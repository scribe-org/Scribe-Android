package be.scri.services

import be.scri.R

class FrenchKeyboardIME : SimpleKeyboardIME() {
    override fun getKeyboardLayoutXML(): Int = R.xml.keys_letters_french
}

