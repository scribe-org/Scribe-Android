// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import android.content.Context
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

class ContractDataLoaderTest {
    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var tempFilesDir: File
    private lateinit var loader: ContractDataLoader

    private val sampleYaml =
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

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        assetManager = mockk(relaxed = true)
        tempFilesDir =
            java.nio.file.Files
                .createTempDirectory("scribe_test_contracts")
                .toFile()
        every { context.filesDir } returns tempFilesDir
        every { context.assets } returns assetManager
        loader = ContractDataLoader(context)
    }

    @AfterEach
    fun tearDown() {
        tempFilesDir.deleteRecursively()
    }

    @Test
    fun loadContract_fromLocalStorage_parsesSuccessfully() {
        val contractsDir = File(tempFilesDir, "data-contracts")
        contractsDir.mkdirs()
        File(contractsDir, "en.yaml").writeText(sampleYaml)

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
    fun loadContract_fallbackToAssets_parsesSuccessfully() {
        every { assetManager.open("data-contracts/en.yaml") } returns ByteArrayInputStream(sampleYaml.toByteArray())

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
    }

    @Test
    fun saveContract_writesFileAndCanBeLoaded() {
        val saved = loader.saveContract("DE", sampleYaml)
        assertTrue(saved)

        val contract = loader.loadContract("DE")
        assertNotNull(contract)
        assertEquals(listOf("gender"), contract?.nouns?.genders?.canonical)
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
    fun parseContract_validYaml_returnsDataContract() {
        val contract = loader.parseContract(sampleYaml)
        assertNotNull(contract)
        assertEquals(listOf("gender"), contract?.nouns?.genders?.canonical)
    }

    @Test
    fun parseContract_invalidYaml_returnsNull() {
        val contract = loader.parseContract("invalid: yaml: [")
        assertNull(contract)
    }
}
