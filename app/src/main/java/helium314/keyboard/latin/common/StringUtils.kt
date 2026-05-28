/*
 * Copyright (C) 2013 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.common

import java.util.Locale

fun CharSequence.codePointAt(offset: Int) = Character.codePointAt(this, offset)
fun CharSequence.codePointBefore(offset: Int) = Character.codePointBefore(this, offset)

/** Loops over the codepoints in [text]. Exits when [loop] returns true */
inline fun loopOverCodePoints(text: CharSequence, loop: (cp: Int, charCount: Int) -> Boolean) {
    var offset = 0
    while (offset < text.length) {
        val cp = text.codePointAt(offset)
        val charCount = Character.charCount(cp)
        if (loop(cp, charCount)) return
        offset += charCount
    }
}

/** Loops backwards over the codepoints in [text]. Exits when [loop] returns true */
inline fun loopOverCodePointsBackwards(text: CharSequence, loop: (cp: Int, charCount: Int) -> Boolean) {
    var offset = text.length
    while (offset > 0) {
        val cp = text.codePointBefore(offset)
        val charCount = Character.charCount(cp)
        if (loop(cp, charCount)) return
        offset -= charCount
    }
}

fun isEmoji(c: Int): Boolean = mightBeEmoji(c) && isEmoji(StringUtils.newSingleCodePointString(c))

fun isEmoji(text: CharSequence): Boolean = mightBeEmoji(text) && text.matches(emoRegex)

fun mightBeEmoji(text: CharSequence): Boolean {
    loopOverCodePoints(text) { cp, _ ->
        if (mightBeEmoji(cp)) return true
        false
    }
    return false
}

fun mightBeEmoji(codePoint: Int): Boolean {
    return StringUtils.mightBeEmoji(codePoint)
}

fun String.decapitalize(locale: Locale): String {
    if (isEmpty() || !this[0].isUpperCase()) return this
    return replaceFirstChar { it.lowercase(locale) }
}

private val emoRegex = Regex("[#*0-9]\\uFE0F?\\u20E3|[\\xA9\\xAE\\u203C\\u2049\\u2122\\u2139\\u2194-\\u2199\\u21A9\\u21AA\\u231A\\u231B\\u2328\\u23CF\\u23ED-\\u23EF\\u23F1\\u23F2\\u23F8-\\u23FA\\u24C2\\u25AA\\u25AB\\u25B6\\u25C0\\u25FB\\u25FC\\u25FE\\u2600-\\u2604\\u260E\\u2611\\u2614\\u2615\\u2618\\u2620\\u2622\\u2623\\u2626\\u262A\\u262E\\u262F\\u2638-\\u263A\\u2640\\u2642\\u2648-\\u2653\\u265F\\u2660\\u2663\\u2665\\u2666\\u2668\\u267B\\u267E\\u267F\\u2692\\u2694-\\u2697\\u2699\\u269B\\u269C\\u26A0\\u26A7\\u26AA\\u26B0\\u26B1\\u26BD\\u26BE\\u26C4\\u26C8\\u26CF\\u26D1\\u26E9\\u26F0-\\u26F5\\u26F7\\u26F8\\u26FA\\u2702\\u2708\\u2709\\u270F\\u2712\\u2714\\u2716\\u271D\\u2721\\u2733\\u2734\\u2744\\u2747\\u2757\\u2763\\u27A1\\u2934\\u2935\\u2B05-\\u2B07\\u2B1B\\u2B1C\\u2B55\\u3030\\u303D\\u3297\\u3299]")
