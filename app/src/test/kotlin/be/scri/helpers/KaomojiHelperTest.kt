// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import be.scri.R
import org.junit.Assert.assertEquals
import org.junit.Test

class KaomojiHelperTest {
    @Test
    fun kaomojiData_holdsPropertiesCorrectly() {
        val kaomoji = KaomojiData("kaomoji_joy", "(•‿•)", "Happy")
        assertEquals("kaomoji_joy", kaomoji.category)
        assertEquals("(•‿•)", kaomoji.kaomoji)
        assertEquals("Happy", kaomoji.name)
    }

    @Test
    fun getCategoryIconRes_returnsKaomojiIcon_forKaomojiCategory() {
        assertEquals(R.drawable.ic_emoji_kaomoji, getCategoryIconRes("kaomoji_joy"))
        assertEquals(R.drawable.ic_emoji_kaomoji, getCategoryIconRes("kaomoji_sad"))
        assertEquals(R.drawable.ic_emoji_kaomoji, getCategoryIconRes("kaomoji_angry"))
        assertEquals(R.drawable.ic_emoji_kaomoji, getCategoryIconRes("kaomoji_shrug"))
    }

    @Test
    fun getCategoryIconRes_returnsDefaultIcons_forStandardCategories() {
        assertEquals(R.drawable.ic_emoji_smileys, getCategoryIconRes("smileys_emotion"))
        assertEquals(R.drawable.ic_emoji_animals, getCategoryIconRes("animals_nature"))
        assertEquals(R.drawable.ic_emoji_vector, getCategoryIconRes("unknown_category"))
    }
}
