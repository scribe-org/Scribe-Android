// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

// Taken from https://gist.github.com/MichaelRocks/1b94bb44c7804e999dbf31dac86955ec.
// Make IMG_5.jpg come before IMG_10.jpg.
// This is a example test, so it is suppressed as it doesn't effect the application.

/**
 * A test class to prepare for application testing.
 */
@Suppress("NestedBlockDepth")
class AlphanumericComparator {
    /**
     * Compares two strings using alphanumeric ordering.
     *
     * @param string1 The first string to compare.
     * @param string2 The second string to compare.
     *
     * @return A negative integer if `string1` comes before `string2`,
     *         a positive integer if `string1` comes after `string2`,
     *         and zero if they are equal.
     */
    fun compare(
        string1: String,
        string2: String,
    ): Int {
        var thisMarker = 0
        var thatMarker = 0
        val s1Length = string1.length
        val s2Length = string2.length

        while (thisMarker < s1Length && thatMarker < s2Length) {
            val thisChunk = getChunk(string1, s1Length, thisMarker)
            thisMarker += thisChunk.length

            val thatChunk = getChunk(string2, s2Length, thatMarker)
            thatMarker += thatChunk.length

            // If both chunks contain numeric characters, sort them numerically.
            var result: Int
            if (isDigit(thisChunk[0]) && isDigit(thatChunk[0])) {
                // Simple chunk comparison by length.
                val thisChunkLength = thisChunk.length
                result = thisChunkLength - thatChunk.length
                // If equal, the first different number counts.
                if (result == 0) {
                    for (i in 0 until thisChunkLength) {
                        result = thisChunk[i] - thatChunk[i]
                        if (result != 0) break
                    }
                }
            } else {
                result = thisChunk.compareTo(thatChunk)
            }

            if (result != 0) {
                return result
            }
        }

        return s1Length - s2Length
    }

    /**
     * Extracts a chunk of characters (either numeric or alphabetic) starting from the given marker.
     *
     * @param string The input string.
     * @param length The total length of the string.
     * @param marker The current position in the string from where extraction starts.
     *
     * @return A substring representing a numeric or alphabetic chunk.
     */
    private fun getChunk(
        string: String,
        length: Int,
        marker: Int,
    ): String {
        var current = marker
        val chunk = StringBuilder()
        var c = string[current]
        chunk.append(c)
        current++
        if (isDigit(c)) {
            while (current < length) {
                c = string[current]
                if (!isDigit(c)) {
                    break
                }
                chunk.append(c)
                current++
            }
        } else {
            while (current < length) {
                c = string[current]
                if (isDigit(c)) {
                    break
                }
                chunk.append(c)
                current++
            }
        }
        return chunk.toString()
    }

    /**
     * Checks if the given character is a numeric digit.
     *
     * @param ch The character to check.
     *
     * @return true if the character is a digit (0-9), false otherwise.
     */
    private fun isDigit(ch: Char) = ch in '0'..'9'
}
