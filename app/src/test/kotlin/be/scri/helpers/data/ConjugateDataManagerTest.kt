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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConjugateDataManagerTest {
    private lateinit var fileManager: DatabaseFileManager
    private lateinit var db: SQLiteDatabase
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

    @BeforeEach
    fun setup() {
        fileManager = mockk(relaxed = true)
        db = mockk(relaxed = true)
        manager = ConjugateDataManager(fileManager)
    }

    private fun createEmptyCursor(): Cursor =
        mockk(relaxed = true) {
            every { moveToFirst() } returns false
        }

    private fun createRowCursor(columnValues: Map<String, String>): Cursor {
        val colNames = columnValues.keys.toTypedArray()
        return mockk(relaxed = true) {
            every { moveToFirst() } returns true
            every { columnNames } returns colNames
            for (i in colNames.indices) {
                val colName = colNames[i]
                every { getString(i) } returns columnValues[colName]
                every { getColumnIndex(colName) } returns i
            }
        }
    }

    private fun createPragmaCursor(columns: List<String>): Cursor {
        val names = columns.toList()
        var index = -1
        return mockk(relaxed = true) {
            every { moveToFirst() } answers {
                index = 0
                names.isNotEmpty()
            }
            every { getColumnIndex("name") } returns 0
            every { getString(0) } answers {
                if (index in names.indices) names[index] else ""
            }
            every { moveToNext() } answers {
                index++
                index < names.size
            }
        }
    }

    private fun setupTablePragmas(columns: List<String>) {
        every { db.rawQuery(match { it.contains("PRAGMA table_info") }, null) } answers {
            createPragmaCursor(columns)
        }
    }

    @Test
    fun `returns null when conjugate database cannot be opened`() {
        every { fileManager.getConjugateDatabase("EN") } returns null

        val result = manager.getTheConjugateLabels("EN", sampleContract, "run")
        assertNull(result)
    }

    @Test
    fun `returns null when verbs table does not exist`() {
        every { fileManager.getConjugateDatabase("EN") } returns db
        val tableCursor = createEmptyCursor()
        every { db.rawQuery(match { it.contains("sqlite_master") }, any()) } returns tableCursor

        val result = manager.getTheConjugateLabels("EN", sampleContract, "run")
        assertNull(result)
    }

    @Test
    fun `finds verb by exact infinitive column and resolves tense forms`() {
        every { fileManager.getConjugateDatabase("EN") } returns db
        val tableCursor = mockk<Cursor>(relaxed = true) { every { moveToFirst() } returns true }
        every { db.rawQuery(match { it.contains("sqlite_master") }, any()) } returns tableCursor
        setupTablePragmas(listOf("infinitive", "simplePresent", "simplePast"))

        val rowCursor =
            createRowCursor(
                mapOf(
                    "infinitive" to "run",
                    "simplePresent" to "runs",
                    "simplePast" to "ran",
                ),
            )
        every { db.rawQuery(match { it.contains("FROM verbs WHERE LOWER") }, any()) } returns rowCursor

        val result = manager.getTheConjugateLabels("EN", sampleContract, "run")
        assertNotNull(result)
        assertEquals("runs", result?.get("Indicative")?.get("Present")?.firstOrNull())
    }

    @Test
    fun `finds verb case-insensitively when user enters uppercase input`() {
        every { fileManager.getConjugateDatabase("DE") } returns db
        val tableCursor = mockk<Cursor>(relaxed = true) { every { moveToFirst() } returns true }
        every { db.rawQuery(match { it.contains("sqlite_master") }, any()) } returns tableCursor
        setupTablePragmas(listOf("infinitive", "simplePresent", "simplePast"))

        val rowCursor =
            createRowCursor(
                mapOf(
                    "infinitive" to "haben",
                    "simplePresent" to "hat",
                    "simplePast" to "hatte",
                ),
            )
        every { db.rawQuery(match { it.contains("FROM verbs WHERE LOWER") }, any()) } returns rowCursor

        val result = manager.getTheConjugateLabels("DE", sampleContract, "HABEN")
        assertNotNull(result)
        assertEquals("hat", result?.get("Indicative")?.get("Present")?.firstOrNull())
    }

    @Test
    fun `finds verb when user inputs an infinitive with 'to ' prefix`() {
        every { fileManager.getConjugateDatabase("EN") } returns db
        val tableCursor = mockk<Cursor>(relaxed = true) { every { moveToFirst() } returns true }
        every { db.rawQuery(match { it.contains("sqlite_master") }, any()) } returns tableCursor
        setupTablePragmas(listOf("infinitive", "simplePresent", "simplePast"))

        val rowCursor =
            createRowCursor(
                mapOf(
                    "infinitive" to "walk",
                    "simplePresent" to "walks",
                    "simplePast" to "walked",
                ),
            )
        // First exact query for "to walk" returns empty, second prefix-stripped query for "walk" succeeds.
        every { db.rawQuery(match { it.contains("FROM verbs WHERE LOWER") }, any()) } returnsMany
            listOf(
                createEmptyCursor(),
                rowCursor,
            )

        val result = manager.getTheConjugateLabels("EN", sampleContract, "to walk")
        assertNotNull(result)
        assertEquals("walks", result?.get("Indicative")?.get("Present")?.firstOrNull())
    }

    @Test
    fun `finds verb by inflected form fallback when user inputs a conjugated verb`() {
        every { fileManager.getConjugateDatabase("EN") } returns db
        val tableCursor = mockk<Cursor>(relaxed = true) { every { moveToFirst() } returns true }
        every { db.rawQuery(match { it.contains("sqlite_master") }, any()) } returns tableCursor
        setupTablePragmas(listOf("infinitive", "simplePresent", "simplePast"))

        val rowCursor =
            createRowCursor(
                mapOf(
                    "infinitive" to "go",
                    "simplePresent" to "goes",
                    "simplePast" to "went",
                ),
            )

        // Infinitive lookup returns empty, fallback inflected search returns row.
        every { db.rawQuery(match { it.contains("SELECT * FROM verbs WHERE LOWER(\"infinitive\")") }, any()) } returns createEmptyCursor()
        every { db.rawQuery(match { it.contains("OR LOWER") }, any()) } returns rowCursor

        val result = manager.getTheConjugateLabels("EN", sampleContract, "went")
        assertNotNull(result)
        assertEquals("goes", result?.get("Indicative")?.get("Present")?.firstOrNull())
    }

    @Test
    fun `getStructuredConjugations returns labels paired with conjugated forms`() {
        every { fileManager.getConjugateDatabase("EN") } returns db
        val tableCursor = mockk<Cursor>(relaxed = true) { every { moveToFirst() } returns true }
        every { db.rawQuery(match { it.contains("sqlite_master") }, any()) } returns tableCursor
        setupTablePragmas(listOf("infinitive", "simplePresent", "simplePast"))

        val rowCursor =
            createRowCursor(
                mapOf(
                    "infinitive" to "speak",
                    "simplePresent" to "speaks",
                    "simplePast" to "spoke",
                ),
            )
        every { db.rawQuery(match { it.contains("FROM verbs WHERE LOWER") }, any()) } returns rowCursor

        val structured = manager.getStructuredConjugations("EN", sampleContract, "speak")
        assertNotNull(structured)
        val pairs = structured?.get("Indicative")?.get("Present")
        assertEquals(2, pairs?.size)
        assertEquals("1ps" to "speaks", pairs?.get(0))
        assertEquals("past" to "spoke", pairs?.get(1))
    }

    @Test
    fun `extractConjugateHeadings returns all form labels and the base word`() {
        val headings = manager.extractConjugateHeadings(sampleContract, "dance")
        assertTrue(headings.contains("1ps"))
        assertTrue(headings.contains("past"))
        assertTrue(headings.contains("dance"))
    }
}
