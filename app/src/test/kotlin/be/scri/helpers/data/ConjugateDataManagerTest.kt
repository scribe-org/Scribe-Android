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
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import be.scri.helpers.DatabaseFileManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
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
@Suppress("LargeClass")
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
        every { fileManager.getConjugateDatabase("DE") } answers { createDatabase() }
        manager = ConjugateDataManager(fileManager)
    }

    private val dummyTranslations =
        Translations(
            wordType =
                WordType(
                    sectionTitle = "",
                    adjective = WordTypeEntry("", ""),
                    adverb = WordTypeEntry("", ""),
                    article = WordTypeEntry("", ""),
                    conjunction = WordTypeEntry("", ""),
                    noun = WordTypeEntry("", ""),
                    postposition = WordTypeEntry("", ""),
                    preposition = WordTypeEntry("", ""),
                    properNoun = WordTypeEntry("", ""),
                    pronoun = WordTypeEntry("", ""),
                    verb = WordTypeEntry("", ""),
                ),
        )

    private val dummyNouns =
        Nouns(
            genders = Genders(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
            numbers = emptyMap(),
        )

    private val createdDatabases = mutableListOf<SQLiteDatabase>()

    private fun createDatabase(): SQLiteDatabase {
        val database = SQLiteDatabase.create(null)
        database.execSQL(
            """
            CREATE TABLE verbs (
                infinitive TEXT,
                simplePresent TEXT,
                pastParticiple TEXT,
                auxiliaryVerb TEXT,
                indicativePresentFirstPersonSingular TEXT,
                wdLexemeId TEXT,
                emptyColumn TEXT
            )
            """.trimIndent(),
        )

        database.execSQL(
            """
            INSERT INTO verbs (
                infinitive, simplePresent, pastParticiple, auxiliaryVerb,
                indicativePresentFirstPersonSingular, wdLexemeId, emptyColumn
            ) VALUES (
                'haben', 'habe', 'gehabt', 'haben', 'habe', 'L1', ''
            )
            """.trimIndent(),
        )

        database.execSQL(
            """
            INSERT INTO verbs (
                infinitive, simplePresent, pastParticiple, auxiliaryVerb,
                indicativePresentFirstPersonSingular, wdLexemeId, emptyColumn
            ) VALUES (
                'essen', 'esse', 'gegessen', 'haben', 'esse', 'L2', ''
            )
            """.trimIndent(),
        )

        database.execSQL(
            """
            INSERT INTO verbs (
                infinitive, simplePresent, pastParticiple, auxiliaryVerb,
                indicativePresentFirstPersonSingular, wdLexemeId, emptyColumn
            ) VALUES (
                'noparticiple', 'test', NULL, 'haben', 'test', 'L3', ''
            )
            """.trimIndent(),
        )

        createdDatabases.add(database)
        return database
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
    fun fallbackToLanguageDatabaseWhenConjugateDatabaseIsNull() {
        every { fileManager.getConjugateDatabase(any()) } returns null
        every { fileManager.getLanguageDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getStructuredConjugations("EN", sampleContract, "walk")
        assertNotNull(result)
        val present = result!!["Indicative"]!!["Present"]!!
        assertEquals(listOf("1ps" to "walks", "past" to "walked"), present)

        val labels = manager.getTheConjugateLabels("EN", sampleContract, "walk")
        assertNotNull(labels)
        assertEquals(listOf("walks", "walked"), labels!!["Indicative"]!!["Present"]!!.toList())
    }

    @Test
    fun fallbackToLanguageDatabaseWhenConjugateDatabaseHasNoVerbsTable() {
        every { fileManager.getConjugateDatabase(any()) } answers { SQLiteDatabase.create(null) }
        every { fileManager.getLanguageDatabase(any()) } answers { createRealDatabase() }

        val result = manager.getStructuredConjugations("EN", sampleContract, "walk")
        assertNotNull(result)
        val present = result!!["Indicative"]!!["Present"]!!
        assertEquals("walks", present.first { it.first == "1ps" }.second)
    }

    @Test
    fun missingVerbsTableInBothDatabasesReturnsNull() {
        val emptyDb1 = SQLiteDatabase.create(null)
        val emptyDb2 = SQLiteDatabase.create(null)
        every { fileManager.getConjugateDatabase(any()) } answers { emptyDb1 }
        every { fileManager.getLanguageDatabase(any()) } answers { emptyDb2 }

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

    @After
    fun tearDown() {
        for (database in createdDatabases) {
            if (database.isOpen) {
                database.close()
            }
        }
        createdDatabases.clear()
    }

    @Test
    fun getConjugationsForVerb_preservesOutOfNumericOrderContractOrder() {
        val contract =
            DataContract(
                nouns = dummyNouns,
                translations = dummyTranslations,
                verbs =
                    Verbs(
                        conjugations =
                            linkedMapOf(
                                2 to
                                    TenseGroup(
                                        sectionTitle = "Group Two",
                                        tenses =
                                            linkedMapOf(
                                                5 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Tense B",
                                                        tenseForms =
                                                            linkedMapOf(
                                                                20 to TenseForm(label = "form20", value = "simplePresent"),
                                                                10 to TenseForm(label = "form10", value = "infinitive"),
                                                            ),
                                                    ),
                                            ),
                                    ),
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Group One",
                                        tenses =
                                            linkedMapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Tense A",
                                                        tenseForms =
                                                            linkedMapOf(
                                                                1 to TenseForm(label = "form1", value = "simplePresent"),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
            )

        val result = manager.getConjugationsForVerb("essen", "DE", contract)
        assertNotNull(result)

        val groupKeys = result!!.keys.toList()
        assertEquals(listOf("Group Two", "Group One"), groupKeys)

        val groupTwoForms = result["Group Two"]!!["Tense B"]!!
        assertEquals(listOf("form20" to "esse", "form10" to "essen"), groupTwoForms)
    }

    @Test
    fun getConjugationsForVerb_filtersEmptyForms() {
        val contract =
            DataContract(
                nouns = dummyNouns,
                translations = dummyTranslations,
                verbs =
                    Verbs(
                        conjugations =
                            linkedMapOf(
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Present",
                                        tenses =
                                            linkedMapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Simple",
                                                        tenseForms =
                                                            linkedMapOf(
                                                                1 to TenseForm(label = "valid", value = "simplePresent"),
                                                                2 to TenseForm(label = "empty", value = "emptyColumn"),
                                                                3 to TenseForm(label = "nonexistent", value = "doesNotExist"),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
            )

        val result = manager.getConjugationsForVerb("essen", "DE", contract)
        assertNotNull(result)

        val forms = result!!["Present"]!!["Simple"]!!
        assertEquals(1, forms.size)
        assertEquals("valid" to "esse", forms[0])
    }

    @Test
    fun getConjugationsForVerb_resolvesComplexFormsCorrectly() {
        val contract =
            DataContract(
                nouns = dummyNouns,
                translations = dummyTranslations,
                verbs =
                    Verbs(
                        conjugations =
                            linkedMapOf(
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Compound",
                                        tenses =
                                            linkedMapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Tense Forms",
                                                        tenseForms =
                                                            linkedMapOf(
                                                                1 to
                                                                    TenseForm(
                                                                        label = "literal",
                                                                        value = "[have] pastParticiple",
                                                                    ),
                                                                2 to
                                                                    TenseForm(
                                                                        label = "dynamicCurly",
                                                                        value = "{indicativePresentFirstPersonSingular auxiliaryVerb} pastParticiple",
                                                                    ),
                                                                3 to
                                                                    TenseForm(
                                                                        label = "dynamicSquare",
                                                                        value = "[indicativePresentFirstPersonSingular auxiliaryVerb] pastParticiple",
                                                                    ),
                                                                4 to
                                                                    TenseForm(
                                                                        label = "missingParticiple",
                                                                        value = "{indicativePresentFirstPersonSingular auxiliaryVerb} emptyColumn",
                                                                    ),
                                                                5 to
                                                                    TenseForm(
                                                                        label = "unknownAuxColumn",
                                                                        value = "{indicativePresentFirstPersonSingular noSuchAux} pastParticiple",
                                                                    ),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
            )

        val result = manager.getConjugationsForVerb("essen", "DE", contract)
        assertNotNull(result)

        val forms = result!!["Compound"]!!["Tense Forms"]!!
        assertEquals(3, forms.size)
        assertEquals("literal" to "have gegessen", forms[0])
        assertEquals("dynamicCurly" to "habe gegessen", forms[1])
        assertEquals("dynamicSquare" to "habe gegessen", forms[2])
        verify(exactly = 1) { fileManager.getConjugateDatabase("DE") }
    }

    @Test
    fun getConjugationsForVerb_doesNotLeakConnection() {
        val realDb = SQLiteDatabase.create(null)
        realDb.execSQL("CREATE TABLE verbs (infinitive TEXT, simplePresent TEXT)")
        realDb.execSQL("INSERT INTO verbs VALUES ('essen', 'esse')")
        val spyDb = spyk(realDb)

        val localFileManager = mockk<DatabaseFileManager>()
        every { localFileManager.getConjugateDatabase("DE") } returns spyDb

        val localManager = ConjugateDataManager(localFileManager)
        val contract =
            DataContract(
                nouns = dummyNouns,
                translations = dummyTranslations,
                verbs =
                    Verbs(
                        conjugations =
                            linkedMapOf(
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Present",
                                        tenses =
                                            linkedMapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Simple",
                                                        tenseForms =
                                                            linkedMapOf(
                                                                1 to TenseForm(label = "1ps", value = "simplePresent"),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
            )

        val result = localManager.getConjugationsForVerb("essen", "DE", contract)
        assertNotNull(result)
        verify { spyDb.close() }
    }

    @Test
    fun getConjugationsForVerb_returnsNullWhenVerbNotFound() {
        val contract =
            DataContract(
                nouns = dummyNouns,
                translations = dummyTranslations,
                verbs =
                    Verbs(
                        conjugations =
                            linkedMapOf(
                                1 to
                                    TenseGroup(
                                        sectionTitle = "Present",
                                        tenses =
                                            linkedMapOf(
                                                1 to
                                                    ConjugationCategory(
                                                        tenseTitle = "Simple",
                                                        tenseForms =
                                                            linkedMapOf(
                                                                1 to TenseForm(label = "form", value = "simplePresent"),
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    ),
            )

        val result = manager.getConjugationsForVerb("unknownverb", "DE", contract)
        assertNull(result)
    }

    @Test
    fun parseComplexForm_closesAuxiliaryDatabaseWhenStandalone() {
        var auxDbClosed = false
        val auxDb = mockk<SQLiteDatabase>(relaxed = true)
        every { auxDb.rawQuery(any(), any()) } returns
            mockk(relaxed = true) {
                every { moveToFirst() } returns true
                every { getString(0) } returns "habe"
            }
        every { auxDb.close() } answers { auxDbClosed = true }

        val cursor =
            mockk<Cursor>(relaxed = true) {
                every { moveToFirst() } returns true
                every { getColumnIndex("pastParticiple") } returns 0
                every { getColumnIndex("auxiliaryVerb") } returns 1
                every { getString(0) } returns "gegessen"
                every { getString(1) } returns "haben"
            }

        val localFileManager = mockk<DatabaseFileManager>()
        every { localFileManager.getConjugateDatabase("DE") } returns auxDb

        val localManager = ConjugateDataManager(localFileManager)
        val result =
            localManager.parseComplexForm(
                cursor,
                "{indicativePresentFirstPersonSingular auxiliaryVerb} pastParticiple",
                "DE",
                existingDb = null,
            )

        assertEquals("habe gegessen", result)
        assertTrue(auxDbClosed)
    }

    @Test
    fun parseComplexForm_returnsEmptyOnSqlNullParticiple() {
        val cursor =
            mockk<Cursor>(relaxed = true) {
                every { getColumnIndex("pastParticiple") } returns 0
                every { getColumnIndex("auxiliaryVerb") } returns 1
                every { getString(0) } returns null
                every { getString(1) } returns "haben"
            }
        val result =
            manager.parseComplexForm(
                cursor,
                "{indicativePresentFirstPersonSingular auxiliaryVerb} pastParticiple",
                "DE",
            )
        assertEquals("", result)
    }

    @Test
    fun parseComplexForm_returnsEmptyOnBlankParticiple() {
        val cursor =
            mockk<Cursor>(relaxed = true) {
                every { getColumnIndex("emptyColumn") } returns 0
                every { getString(0) } returns "   "
            }
        val result =
            manager.parseComplexForm(
                cursor,
                "[have] emptyColumn",
                "DE",
            )
        assertEquals("", result)
    }

    @Test
    fun parseComplexForm_multiWordLiteralBracketYieldsCombinedForm() {
        val cursor =
            mockk<Cursor>(relaxed = true) {
                every { getColumnIndex("pastParticiple") } returns 0
                every { getColumnIndex("have") } returns -1
                every { getString(0) } returns "walked"
            }
        val result =
            manager.parseComplexForm(
                cursor,
                "[will have] pastParticiple",
                "EN",
            )
        assertEquals("will have walked", result)
    }
}
