// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import android.content.res.AssetManager
import be.scri.R
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class EmojiHelperTest {

    @Test
    fun parseRawEmojiSpecsFile_validFile_parsesCorrectly() {
        val context = mockk<Context>()
        val assetManager = mockk<AssetManager>()
        val specContent = """
            # Comment line
            [smileys_emotion]
            😀;;
            😃;;
            
            [people_body]
            👋;;
            	👋🏻;;
            	👋🏼;;
        """.trimIndent()
        val inputStream = ByteArrayInputStream(specContent.toByteArray())

        every { context.assets } returns assetManager
        every { assetManager.open("emoji_spec.txt") } returns inputStream

        val result = parseRawEmojiSpecsFile(context, "emoji_spec.txt")

        assertEquals(3, result.size)

        assertEquals("smileys_emotion", result[0].category)
        assertEquals("😀", result[0].emoji)
        assertEquals(0, result[0].variants.size)

        assertEquals("smileys_emotion", result[1].category)
        assertEquals("😃", result[1].emoji)
        assertEquals(0, result[1].variants.size)

        assertEquals("people_body", result[2].category)
        assertEquals("👋", result[2].emoji)
        assertEquals(listOf("👋🏻", "👋🏼"), result[2].variants)
    }

    @Test
    fun parseRawEmojiSpecsFile_emptyAndInvalidLines_ignoresThem() {
        val context = mockk<Context>()
        val assetManager = mockk<AssetManager>()
        val specContent = """
            [cat1]
            
            # comment
            emoji1;;
            
            invalid_line
            emoji2;;
        """.trimIndent()
        val inputStream = ByteArrayInputStream(specContent.toByteArray())

        every { context.assets } returns assetManager
        every { assetManager.open("emoji_spec.txt") } returns inputStream

        val result = parseRawEmojiSpecsFile(context, "emoji_spec.txt")

        assertEquals(2, result.size)
        assertEquals("emoji1", result[0].emoji)
        assertEquals("emoji2", result[1].emoji)
    }

    @Test
    fun parseRawEmojiSpecsFile_noCategory_defaultsToNone() {
        val context = mockk<Context>()
        val assetManager = mockk<AssetManager>()
        val specContent = """
            emoji1;;
        """.trimIndent()
        val inputStream = ByteArrayInputStream(specContent.toByteArray())

        every { context.assets } returns assetManager
        every { assetManager.open("emoji_spec.txt") } returns inputStream

        val result = parseRawEmojiSpecsFile(context, "emoji_spec.txt")

        assertEquals(1, result.size)
        assertEquals("none", result[0].category)
        assertEquals("emoji1", result[0].emoji)
    }

    @Test
    fun getCategoryIconRes_knownCategories_returnsCorrectIcon() {
        assertEquals(R.drawable.ic_emoji_smileys, getCategoryIconRes("smileys_emotion"))
        assertEquals(R.drawable.ic_emoji_people, getCategoryIconRes("people_body"))
        assertEquals(R.drawable.ic_emoji_animals, getCategoryIconRes("animals_nature"))
        assertEquals(R.drawable.ic_emoji_food, getCategoryIconRes("food_drink"))
        assertEquals(R.drawable.ic_emoji_travel, getCategoryIconRes("travel_places"))
        assertEquals(R.drawable.ic_emoji_activities, getCategoryIconRes("activities"))
        assertEquals(R.drawable.ic_emoji_objects, getCategoryIconRes("objects"))
        assertEquals(R.drawable.ic_emoji_symbols, getCategoryIconRes("symbols"))
        assertEquals(R.drawable.ic_emoji_flags, getCategoryIconRes("flags"))
        assertEquals(R.drawable.counter_clockwise_icon, getCategoryIconRes("recently_used"))
    }

    @Test
    fun getCategoryIconRes_unknownCategory_returnsDefaultIcon() {
        assertEquals(R.drawable.ic_emoji_vector, getCategoryIconRes("unknown_category"))
    }

    @Test
    fun parseRawEmojiSpecsFile_firstLineIsVariant_treatsAsBase() {
        val context = mockk<Context>()
        val assetManager = mockk<AssetManager>()
        val specContent = "\tvariant1;;\n"
        val inputStream = ByteArrayInputStream(specContent.toByteArray())

        every { context.assets } returns assetManager
        every { assetManager.open("emoji_spec.txt") } returns inputStream

        val result = parseRawEmojiSpecsFile(context, "emoji_spec.txt")

        assertEquals(1, result.size)
        assertEquals("variant1", result[0].emoji)
        assertEquals(0, result[0].variants.size)
    }
}
