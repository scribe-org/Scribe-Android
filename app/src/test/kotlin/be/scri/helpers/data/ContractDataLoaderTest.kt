// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.content.Context
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class ContractDataLoaderTest {
    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var loader: ContractDataLoader

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        assetManager = mockk(relaxed = true)
        every { context.assets } returns assetManager
        loader = ContractDataLoader(context)
    }

    @Test
    fun loadContract_validYaml_parsesNounsVerbsTranslations() {
        val yamlContent =
            """
            nouns:
              genders:
                canonical: [gender]
                feminines: []
                masculines: []
                commons: []
                neuters: []
              numbers:
                1:
                  singular: nominativeSingular
                  plural: nominativePlural
            verbs:
              conjugations:
                1:
                  sectionTitle: Present
                  tenses:
                    1:
                      tenseTitle: Pr. Simple
                      tenseForms:
                        1:
                          label: I/you/plural
                          value: simplePresent
            translations:
              wordType:
                sectionTitle: Word Types
                adjective:
                  displayValue: Adjective
                  sectionTitle: Adjectives
                adverb:
                  displayValue: Adverb
                  sectionTitle: Adverbs
                article:
                  displayValue: Article
                  sectionTitle: Articles
                conjunction:
                  displayValue: Conjunction
                  sectionTitle: Conjunctions
                noun:
                  displayValue: Noun
                  sectionTitle: Nouns
                postposition:
                  displayValue: Postposition
                  sectionTitle: Postpositions
                preposition:
                  displayValue: Preposition
                  sectionTitle: Prepositions
                proper_noun:
                  displayValue: Proper Noun
                  sectionTitle: Proper Nouns
                pronoun:
                  displayValue: Pronoun
                  sectionTitle: Pronouns
                verb:
                  displayValue: Verb
                  sectionTitle: Verbs
            """.trimIndent()

        every { assetManager.open("data-contracts/en.yaml") } returns ByteArrayInputStream(yamlContent.toByteArray())

        val contract = loader.loadContract("EN")

        assertNotNull(contract)
        assertEquals(listOf("gender"), contract?.nouns?.genders?.canonical)
        assertEquals(
            "nominativeSingular",
            contract
                ?.nouns
                ?.numbers
                ?.get(1)
                ?.singular,
        )
        assertEquals(
            "nominativePlural",
            contract
                ?.nouns
                ?.numbers
                ?.get(1)
                ?.plural,
        )
        assertEquals(
            "Present",
            contract
                ?.verbs
                ?.conjugations
                ?.get(1)
                ?.sectionTitle,
        )
        assertEquals("Word Types", contract?.translations?.wordType?.sectionTitle)
    }

    @Test
    fun loadContract_fileNotFound_returnsNull() {
        every { assetManager.open(any()) } throws IOException("File not found")

        val contract = loader.loadContract("NON_EXISTENT")

        assertNull(contract)
    }

    @Test
    fun loadContract_malformedYaml_returnsNull() {
        val invalidYaml = "nouns: [invalid: yaml: :"
        every { assetManager.open(any()) } returns ByteArrayInputStream(invalidYaml.toByteArray())

        val contract = loader.loadContract("EN")

        assertNull(contract)
    }

    @Test
    fun allAssetContracts_parseSuccessfully() {
        val languages = listOf("de", "en", "es", "fr", "it", "pt", "ru", "sv")
        for (lang in languages) {
            val file = java.io.File("src/main/assets/data-contracts/$lang.yaml")
            val content = file.readText()
            every { assetManager.open("data-contracts/$lang.yaml") } returns ByteArrayInputStream(content.toByteArray())

            val contract = loader.loadContract(lang)
            assertNotNull(contract, "Failed to parse contract for language: $lang")
            assertNotNull(contract?.nouns, "Nouns is null for language: $lang")
            assertNotNull(contract?.verbs, "Verbs is null for language: $lang")
            assertNotNull(contract?.translations, "Translations is null for language: $lang")
            assertNotNull(contract?.nouns?.numbers, "Numbers is null for language: $lang")
            assertNotNull(contract?.verbs?.conjugations, "Conjugations is null for language: $lang")
        }
    }
}
