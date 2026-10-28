// SPDX-License-Identifier: GPL-3.0-or-later
package be.scri.helpers.data

import ConjugationCategory
import DataContract
import Genders
import Nouns
import TenseForm
import TenseGroup
import Translations
import Verbs
import WordType
import WordTypeEntry
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConjugateDataManagerTest {
    private lateinit var fileManager: DatabaseFileManager
    private lateinit var manager: ConjugateDataManager

    private val sampleContract =
        DataContract(
            nouns = Nouns(Genders(listOf("gender"), emptyList(), emptyList(), emptyList(), emptyList()), emptyMap()),
            verbs =
                Verbs(
                    conjugations =
                        mapOf(
                            1 to
                                TenseGroup(
                                    sectionTitle = "Indicative",
                                    tenses =
                                        mapOf(
                                            1 to
                                                ConjugationCategory(
                                                    tenseTitle = "Present",
                                                    tenseForms =
                                                        mapOf(
                                                            1 to TenseForm(label = "1ps", value = "simplePresent"),
                                                            2 to TenseForm(label = "past", value = "simplePast"),
                                                        ),
                                                ),
                                        ),
                                ),
                        ),
                ),
            translations =
                Translations(
                    wordType =
                        WordType(
                            sectionTitle = "Word Types",
                            adjective = WordTypeEntry("adj", "Adjective"),
                            adverb = WordTypeEntry("adv", "Adverb"),
                            article = WordTypeEntry("art", "Article"),
                            conjunction = WordTypeEntry("conj", "Conjunction"),
                            noun = WordTypeEntry("n", "Noun"),
                            postposition = WordTypeEntry("post", "Postposition"),
                            preposition = WordTypeEntry("prep", "Preposition"),
                            properNoun = WordTypeEntry("prop", "Proper Noun"),
                            pronoun = WordTypeEntry("pron", "Pronoun"),
                            verb = WordTypeEntry("v", "Verb"),
                        ),
                ),
        )

    private fun createRealDatabase(): SQLiteDatabase {
        val db = SQLiteDatabase.create(null)
        db.execSQL(
            """
            CREATE TABLE verbs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                infinitive TEXT,
                simplePresent TEXT,
                simplePast TEXT,
                pastParticiple TEXT,
                wdLexemeId TEXT,
                auxiliaryVerb TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO verbs (infinitive, simplePresent, simplePast, pastParticiple, wdLexemeId, auxiliaryVerb)
            VALUES ('walk', 'walks', 'walked', 'walked', 'L100', 'have')
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO verbs (infinitive, simplePresent, simplePast, pastParticiple, wdLexemeId, auxiliaryVerb)
            VALUES ('haben', 'habe', 'hatte', 'gehabt', 'L200', '')
            """.trimIndent(),
        )
        return db
    }

    @Before
    fun setup() {
        fileManager = mockk(relaxed = true)
        manager = ConjugateDataManager(fileManager)
    }

    @Test
    fun getTheConjugateLabelsReturnsCorrectStructureWithRealDatabase() {
        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getTheConjugateLabels("EN", sampleContract, "walk")

        assertNotNull(result)
        val indicative = result!!["Indicative"]
        assertNotNull(indicative)
        val present = indicative!!["Present"]
        assertNotNull(present)
        assertEquals(listOf("walks", "walked"), present!!.toList())
    }

    @Test
    fun getStructuredConjugationsReturnsLabelsAndFormsWithRealDatabase() {
        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getStructuredConjugations("EN", sampleContract, "walk")

        assertNotNull(result)
        val indicative = result!!["Indicative"]
        assertNotNull(indicative)
        val present = indicative!!["Present"]
        assertNotNull(present)
        assertEquals(listOf("1ps" to "walks", "past" to "walked"), present)
    }

    @Test
    fun caseInsensitiveLookupFindsUppercaseVerb() {
        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getStructuredConjugations("EN", sampleContract, "WALK")

        assertNotNull(result)
        val present = result!!["Indicative"]!!["Present"]!!
        assertEquals("walks", present.first { it.first == "1ps" }.second)
    }

    @Test
    fun prefixStrippingLookupFindsPrefixedVerb() {
        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getStructuredConjugations("EN", sampleContract, "to walk")

        assertNotNull(result)
        val present = result!!["Indicative"]!!["Present"]!!
        assertEquals("walks", present.first { it.first == "1ps" }.second)
    }

    @Test
    fun inflectedFormLookupFindsBaseVerbRow() {
        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        // User typed "walked" (inflected past form), fallback search should locate the verb row
        val result = manager.getStructuredConjugations("EN", sampleContract, "walked")

        assertNotNull(result)
        val present = result!!["Indicative"]!!["Present"]!!
        assertEquals("walks", present.first { it.first == "1ps" }.second)
    }

    @Test
    fun parseComplexFormWithDynamicAuxiliaryResolvesCorrectly() {
        val contractWithComplex =
            DataContract(
                nouns = sampleContract.nouns,
                verbs =
                    Verbs(
                        conjugations =
                            mapOf(
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Perfect",
                                        tenses =
                                            mapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Present Perfect",
                                                        tenseForms =
                                                            mapOf(
                                                                1 to
                                                                    TenseForm(
                                                                        label = "1ps",
                                                                        value = "[simplePresent auxiliaryVerb] pastParticiple",
                                                                    ),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
                translations = sampleContract.translations,
            )

        // Setup a database where auxiliary verb references wdLexemeId L200 ('haben')
        val db = SQLiteDatabase.create(null)
        db.execSQL(
            """
            CREATE TABLE verbs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                infinitive TEXT,
                simplePresent TEXT,
                pastParticiple TEXT,
                wdLexemeId TEXT,
                auxiliaryVerb TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO verbs (infinitive, simplePresent, pastParticiple, wdLexemeId, auxiliaryVerb)
            VALUES ('gehen', 'gehe', 'gegangen', 'L300', 'L200')
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO verbs (infinitive, simplePresent, pastParticiple, wdLexemeId, auxiliaryVerb)
            VALUES ('haben', 'habe', 'gehabt', 'L200', '')
            """.trimIndent(),
        )

        every { fileManager.getConjugateDatabase(any()) } answers { db }

        val result = manager.getStructuredConjugations("DE", contractWithComplex, "gehen")

        assertNotNull(result)
        val form = result!!["Perfect"]!!["Present Perfect"]!!.first().second
        assertEquals("habe gegangen", form)
    }

    @Test
    fun missingVerbColumnInComplexFormReturnsEmptyStringNotJustAuxiliary() {
        val contractWithComplex =
            DataContract(
                nouns = sampleContract.nouns,
                verbs =
                    Verbs(
                        conjugations =
                            mapOf(
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Perfect",
                                        tenses =
                                            mapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Future",
                                                        tenseForms =
                                                            mapOf(
                                                                1 to
                                                                    TenseForm(
                                                                        label = "1ps",
                                                                        value = "[will] nonexistentColumn",
                                                                    ),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
                translations = sampleContract.translations,
            )

        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        val singleForm = manager.getTheValueForTheConjugateWord("walk", "[will] nonexistentColumn", "EN")
        assertEquals("", singleForm)

        val result = manager.getStructuredConjugations("EN", contractWithComplex, "walk")
        // Since the only form in the category produced "", the category/group is omitted
        assertNull(result)
    }

    @Test
    fun missingVerbReturnsNull() {
        every { fileManager.getConjugateDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getStructuredConjugations("EN", sampleContract, "nonexistent")
        assertNull(result)
    }

    @Test
    fun missingVerbsTableReturnsNull() {
        val emptyDb = SQLiteDatabase.create(null)
        every { fileManager.getConjugateDatabase(any()) } answers { emptyDb }

        val result = manager.getStructuredConjugations("EN", sampleContract, "walk")
        assertNull(result)
    }

    @Test
    fun extractConjugateHeadingsCollectsAllFormLabelsAndWord() {
        val headings = manager.extractConjugateHeadings(sampleContract, "run")
        assertTrue(headings.contains("1ps"))
        assertTrue(headings.contains("past"))
        assertTrue(headings.contains("run"))
    }
}
