// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import android.content.Context
import android.content.SharedPreferences
import be.scri.R
import be.scri.models.ScribeState
import be.scri.services.GeneralKeyboardIME
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConjugationHandlerTest {
    private val ime = mockk<GeneralKeyboardIME>(relaxed = true)
    private val dataHandler = KeyboardDataHandler()
    private val context = mockk<Context>(relaxed = true)
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private lateinit var handler: ConjugationHandler

    @Before
    fun setUp() {
        every { ime.dataHandler } returns dataHandler
        every { ime.applicationContext } returns context
        every { ime.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        handler = ConjugationHandler(ime)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun applyCapitalizationToConjugations_firstLetterCapitalization() {
        val input: MutableMap<String, MutableMap<String, Collection<String>>> =
            mutableMapOf(
                "Present" to mutableMapOf("I" to listOf("go", "run")),
            )

        val result = handler.applyCapitalizationToConjugations(input, isAllCaps = false)

        assertEquals(listOf("Go", "Run"), result["Present"]?.get("I"))
    }

    @Test
    fun applyCapitalizationToConjugations_allCapsFormat() {
        val input: MutableMap<String, MutableMap<String, Collection<String>>> =
            mutableMapOf(
                "Present" to mutableMapOf("I" to listOf("go", "run")),
            )

        val result = handler.applyCapitalizationToConjugations(input, isAllCaps = true)

        assertEquals(listOf("GO", "RUN"), result["Present"]?.get("I"))
    }

    @Test
    fun getValidatedConjugateIndex_clampsToValidRange() {
        every { sharedPrefs.getInt("conjugate_index", 0) } returns 5
        handler.conjugateOutput =
            mutableMapOf(
                "Present" to mutableMapOf(),
                "Past" to mutableMapOf(),
            )

        val index = handler.getValidatedConjugateIndex()

        // Max index is 1 (size 2 - 1)
        assertEquals(1, index)
    }

    @Test
    fun saveConjugateModeType_spanishReturns3x2() {
        handler.saveConjugateModeType("es", isSubsequent = false)

        verify { editor.putString("conjugate_mode_type", "3x2") }
    }

    @Test
    fun saveConjugateModeType_englishReturns2x2() {
        handler.saveConjugateModeType("en", isSubsequent = false)

        verify { editor.putString("conjugate_mode_type", "2x2") }
    }

    @Test
    fun getKeyboardLayoutForState_selectVerbConjunction_returnsCorrectXml() {
        every { ime.language } returns "en"
        every { ime.defaultConjugateLayoutXML } returns R.xml.conjugate_view_3x2

        // Default layout when no data size
        val defaultXml = handler.getKeyboardLayoutForState(ScribeState.SELECT_VERB_CONJUNCTION, isSubsequentArea = false, dataSize = 0)
        assertEquals(R.xml.conjugate_view_3x2, defaultXml)

        // 2x1 layout for data size 2
        val size2Xml = handler.getKeyboardLayoutForState(ScribeState.SELECT_VERB_CONJUNCTION, isSubsequentArea = true, dataSize = 2)
        assertEquals(R.xml.conjugate_view_2x1, size2Xml)

        // 1x3 layout for data size 3
        val size3Xml = handler.getKeyboardLayoutForState(ScribeState.SELECT_VERB_CONJUNCTION, isSubsequentArea = true, dataSize = 3)
        assertEquals(R.xml.conjugate_view_1x3, size3Xml)
    }
}
