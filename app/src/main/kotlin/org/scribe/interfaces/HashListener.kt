package org.scribe.interfaces

interface HashListener {
    fun receivedHash(hash: String, type: Int)
}
