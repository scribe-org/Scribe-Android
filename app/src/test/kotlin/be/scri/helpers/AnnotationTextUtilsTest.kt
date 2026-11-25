// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers

import android.content.Context
import be.scri.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class AnnotationTextUtilsTest {
    private val context = mockk<Context>()

    @Test
    fun testGetLocalizedKeywordCaseInsensitivity() {
        assertEquals("Acc", AnnotationTextUtils.getLocalizedKeyword("English", "accusative case"))
        assertEquals("Acc", AnnotationTextUtils.getLocalizedKeyword("English", "Accusative Case"))
        assertEquals("Acc", AnnotationTextUtils.getLocalizedKeyword("English", "ACCUSATIVE CASE"))
        assertEquals("Dat", AnnotationTextUtils.getLocalizedKeyword("English", "dative case"))
        assertEquals("Gen", AnnotationTextUtils.getLocalizedKeyword("English", "genitive case"))
    }

    @Test
    fun testGetLocalizedKeywordRussianConversions() {
        // prepAnnotationConversionDict mapping for Russian:
        // "Acc" to "Вин", "Dat" to "Дат", "Gen" to "Род", "Loc" to "Мес", "Pre" to "Пре", "Ins" to "Инс"
        assertEquals("Вин", AnnotationTextUtils.getLocalizedKeyword("Russian", "accusative case"))
        assertEquals("Дат", AnnotationTextUtils.getLocalizedKeyword("Russian", "dative case"))
        assertEquals("Род", AnnotationTextUtils.getLocalizedKeyword("Russian", "genitive case"))
        assertEquals("Мес", AnnotationTextUtils.getLocalizedKeyword("Russian", "locative case"))
        assertEquals("Пре", AnnotationTextUtils.getLocalizedKeyword("Russian", "prepositional case"))
        assertEquals("Инс", AnnotationTextUtils.getLocalizedKeyword("Russian", "instrumental case"))
    }

    @Test
    fun testHandleTextForCaseAnnotation() {
        every { context.getString(R.string.i18n_app_keyboard_suggestion) } returns "Suggestion"
        val result = AnnotationTextUtils.handleTextForCaseAnnotation("dative case", "English", context)
        assertEquals(R.color.annotateDeclension, result.first)
        assertEquals("Dat", result.second)

        val resultGerman = AnnotationTextUtils.handleTextForCaseAnnotation("accusative case", "German", context)
        assertEquals(R.color.annotateDeclension, resultGerman.first)
        assertEquals("Akk", resultGerman.second)
    }

    @Test
    fun testHandleColorAndTextForNounType() {
        every { context.getString(R.string.i18n_app_keyboard_suggestion) } returns "Suggestion"

        val resultPlural = AnnotationTextUtils.handleColorAndTextForNounType("PL", "English", context)
        assertEquals(R.color.annotateOrange, resultPlural.first)
        assertEquals("PL", resultPlural.second)

        val resultMasc = AnnotationTextUtils.handleColorAndTextForNounType("masculine", "English", context)
        assertEquals(R.color.annotateBlue, resultMasc.first)
        assertEquals("M", resultMasc.second)

        val resultMascRu = AnnotationTextUtils.handleColorAndTextForNounType("Masculine", "Russian", context)
        assertEquals(R.color.annotateBlue, resultMascRu.first)
        assertEquals("М", resultMascRu.second)
    }
}
