// SPDX-License-Identifier: GPL-3.0-or-later
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

        every { mockPreferences.getString(KEY_RECENT, "") } returns "emoji1,emoji2"
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns mockPreferences

        assertEquals(listOf("emoji1", "emoji2"), getRecentEmojis(context))
    }

    @Test
    fun getRecentEmojis_returnsEmptyList_whenRecentEmojiListDoesNotExist() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()

        every { mockPreferences.getString(KEY_RECENT, "") } returns ""
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns mockPreferences

        assertEquals(emptyList<String>(), getRecentEmojis(context))
    }

    @Test
    fun getRecentEmojis_returnsCorrectList_whenRecentEmojiListIsNull() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()

        every { mockPreferences.getString(KEY_RECENT, "") } returns null
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns mockPreferences

        assertEquals(emptyList<String>(), getRecentEmojis(context))
    }

    @Test
    fun recordRecentEmojis_addsEmojiToRecentList_whenEmojiDoesNotExist() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { mockPreferences.getString(KEY_RECENT, "") } returns "emoji1,emoji2"
        every { mockPreferences.edit() } returns mockEditor
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns mockPreferences

        recordRecentEmoji(context, "emoji3")

        verify { mockEditor.putString(KEY_RECENT, "emoji3,emoji1,emoji2") }
    }

    @Test
    fun recordRecentEmojis_removesOldestEmoji_whenRecentListIsFull() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        val initialList = (1..30).joinToString(",") { "e$it" }
        val expectedList = "new," + (1..29).joinToString(",") { "e$it" }

        every { mockPreferences.getString(KEY_RECENT, "") } returns initialList
        every { mockPreferences.edit() } returns mockEditor
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns mockPreferences

        recordRecentEmoji(context, "new")

        verify { mockEditor.putString(KEY_RECENT, expectedList) }
    }

    @Test
    fun recordRecentEmojis_addEmojiToRecentList_whenExistingRecentEmojisNull() {
        val context = mockk<Context>()
        val mockPreferences = mockk<SharedPreferences>()
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        every { mockPreferences.getString(KEY_RECENT, "") } returns null
        every { mockPreferences.edit() } returns mockEditor
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns mockPreferences

        recordRecentEmoji(context, "emoji1")

        verify { mockEditor.putString(KEY_RECENT, "emoji1") }
    }
}
