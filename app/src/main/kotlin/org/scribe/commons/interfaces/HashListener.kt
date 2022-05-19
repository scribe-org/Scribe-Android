package org.scribe.commons.interfaces

interface HashListener {
    fun receivedHash(hash: String, type: Int)
}
