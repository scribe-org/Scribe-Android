// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")
/**
 * Testing for AlphanumericComparator.
 */

package helpers

import be.scri.helpers.AlphanumericComparator
import org.junit.Assert.assertEquals
import org.junit.Test

class AlphanumericComparatorTest {
    private val subject = AlphanumericComparator()

    @Test
    fun testCompare() {
        assertEquals(-1, subject.compare("IMG_10.png", "IMG_11.png"))

        assertEquals(0, subject.compare("IMG_10.png", "IMG_10.png"))

        assertEquals(1, subject.compare("IMG_11.png", "IMG_10.png"))
    }
}
