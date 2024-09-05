package be.scri.interfaces

interface HashListener {
    fun receivedHash(
        hash: String,
        type: Int,
    )
}
