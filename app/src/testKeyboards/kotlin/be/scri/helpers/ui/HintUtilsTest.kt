// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.ui

import android.content.Context
import be.scri.helpers.english.ENInterfaceVariables
import be.scri.helpers.french.FRInterfaceVariables
import be.scri.helpers.german.DEInterfaceVariables
import be.scri.helpers.italian.ITInterfaceVariables
import be.scri.helpers.portuguese.PTInterfaceVariables
import be.scri.helpers.russian.RUInterfaceVariables
import be.scri.helpers.spanish.ESInterfaceVariables
import be.scri.helpers.swedish.SVInterfaceVariables
import be.scri.models.ScribeState
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class HintUtilsTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun getPromptText_textNullAndSelectVerbConjugation_expectEmptyString() {
        val promptText = HintUtils.getPromptText(ScribeState.SELECT_VERB_CONJUNCTION, "English", context, null)
        assertEquals("", promptText)
    }

    @Test
    fun getPromptText_textNotNullAndSelectVerbConjugation_expectText() {
        val promptText = HintUtils.getPromptText(ScribeState.SELECT_VERB_CONJUNCTION, "English", context, "text")
        assertEquals("text", promptText)
    }

    @ParameterizedTest
    @MethodSource("provideLanguagesAndPluralPrompts")
    fun getPromptText_selectPlural_expectPluralPrompt(language: String, expectedPrompt: String) {
        val promptText = HintUtils.getPromptText(ScribeState.PLURAL, language, context, null)
        assertEquals(expectedPrompt, promptText)
    }

    @Test
    fun getPromptText_selectPluralAndLanguageIsInvalid_expectEnglishDefault() {
        val promptText = HintUtils.getPromptText(ScribeState.PLURAL, "FAKEISH", context, null)
        assertEquals("Plural :", promptText)
    }

    companion object {
        @JvmStatic
        fun provideLanguagesAndPluralPrompts(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("English", ENInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("French", FRInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("German", DEInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("Italian", ITInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("Portuguese", PTInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("Russian", RUInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("Spanish", ESInterfaceVariables.PLURAL_PROMPT),
                Arguments.of("Swedish", SVInterfaceVariables.PLURAL_PROMPT),
            )
        }
    }
}
