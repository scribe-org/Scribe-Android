package be.scri.helpers

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecentEmojiHelperTest {

    @Test
    fun getRecentEmojis_returnsCorrectList_whenRecentEmojiListExists() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()

        every { mockPreferences.getString("recent_emoji_list", "") } returns "emoji1,emoji2"
        every { context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE) } returns mockPreferences

        assertEquals(listOf("emoji1", "emoji2"), getRecentEmojis(context))
    }

    @Test
    fun getRecentEmojis_returnsEmptyList_whenRecentEmojiListDoesNotExist() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()

        every { mockPreferences.getString("recent_emoji_list", "") } returns ""
        every { context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE) } returns mockPreferences

        assertEquals(emptyList<String>(), getRecentEmojis(context))
    }

    @Test
    fun getRecentEmojis_returnsCorrectList_whenRecentEmojiListIsNull() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()

        every { mockPreferences.getString("recent_emoji_list", "") } returns null
        every { context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE) } returns mockPreferences

        assertEquals(emptyList<String>(), getRecentEmojis(context))
    }

    @Test
    fun recordRecentEmojis_addsEmojiToRecentList_whenEmojiDoesNotExist() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { mockPreferences.getString("recent_emoji_list", "") } returns "emoji1,emoji2"
        every { mockPreferences.edit() } returns mockEditor
        every { context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE) } returns mockPreferences

        recordRecentEmoji(context, "emoji3")

        verify { mockEditor.putString("recent_emoji_list", "emoji3,emoji1,emoji2") }
    }

    @Test
    fun recordRecentEmojis_removesOldestEmoji_whenRecentListIsFull() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        // Create a list of 30 emojis: "e1,e2,...,e30"
        val initialList = (1..30).joinToString(",") { "e$it" }
        // Adding "new" should push "e30" out. Result: "new,e1,e2,...,e29"
        val expectedList = "new," + (1..29).joinToString(",") { "e$it" }

        every { mockPreferences.getString("recent_emoji_list", "") } returns initialList
        every { mockPreferences.edit() } returns mockEditor
        every { context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE) } returns mockPreferences

        recordRecentEmoji(context, "new")

        verify { mockEditor.putString("recent_emoji_list", expectedList) }
    }

    @Test
    fun recordRecentEmojis_doesNotAddDuplicateEmoji_whenRecentEmojisNull() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { mockPreferences.getString("recent_emoji_list", "") } returns null
        every { mockPreferences.edit() } returns mockEditor
        every { context.getSharedPreferences("recent_emojis", Context.MODE_PRIVATE) } returns mockPreferences

        recordRecentEmoji(context, "emoji1")

        verify(exactly = 0) { mockEditor.putString(any(), any()) }
    }
}
