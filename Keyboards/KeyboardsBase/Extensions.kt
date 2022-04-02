/**
 * Extensions.kt
 *
 * Extensions for Scribe keyboards.
 */

/**
 * Extension to access the second to last element of an array.
 */
internal fun Array.secondToLast() : Element? {
    if (this.count < 2) {
        return null
    }
    val index = this.count - 2
    return this[index]
}

/**
 * Extensions to String to allow for easier indexing, substring extraction and checking for certain characteristics.
 */
internal fun String.index(fromIdx: Int) : Index =
    this.index(startIndex, offsetBy = fromIdx)

internal fun String.substring(fromIdx: Int) : String {
    val fromIndex = index(fromIdx = fromIdx)
    return String(this[fromIndex...])
}

internal fun String.substring(toIdx: Int) : String {
    val toIndex = index(fromIdx = toIdx)
    return String(this[..<toIndex])
}

internal fun String.substring(range: Range<Int>) : String {
    val startIndex = index(fromIdx = range.lowerBound)
    val endIndex = index(fromIdx = range.upperBound)
    return String(this[startIndex until endIndex])
}

internal fun String.insertPriorToCursor(char: String) : String =
    substring(toIdx = this.count - 1) + char + commandCursor

internal fun String.deletePriorToCursor() : String =
    substring(toIdx = this.count - 2) + commandCursor
internal val String.isLowercase: Boolean
    get() = this == this.lowercased()
internal val String.isUppercase: Boolean
    get() = this == this.uppercased()

internal val String.count(char: String) : Int
    get() = this == this.reduce(0) {
        $1 == char ? $0 + 1 : $0
    }
