package be.scri.models

data class BlockedNumber(
    val id: Long,
    val number: String,
    val normalizedNumber: String,
    val numberToCompare: String,
)
