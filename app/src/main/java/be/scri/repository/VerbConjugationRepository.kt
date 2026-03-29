// SPDX-License-Identifier: GPL-3.0-or-later
@file:Suppress("ktlint:standard:kdoc")

package be.scri.repository

import Conjugation
import ContractDataLoader
import DataContract
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import be.scri.helpers.DatabaseFileManager
import java.io.Closeable
import java.util.TreeMap

/**
 * Loads verb rows from the on-device language SQLite DB, expands them into normalized rows using
 * the JSON data contract (each conjugation [Conjugation.title] is the tense), then stores them in an
 * in-memory SQLite database so filtering uses standard SQL including [WHERE tense = ?].
 */
class VerbConjugationRepository(
    private val context: Context,
) {
    private val fileManager = DatabaseFileManager(context)
    private val contractLoader = ContractDataLoader(context)

    /**
     * Prepares an in-memory DB for one verb. Caller must [VerbConjugationSession.close] when done.
     */
    fun openSession(
        dbLanguageAlias: String,
        lemma: String,
    ): VerbConjugationSession? {
        val trimmed = lemma.trim()
        if (trimmed.isEmpty()) return null

        fileManager.loadDatabaseFile(dbLanguageAlias)
        val dbPath = context.getDatabasePath("${dbLanguageAlias}LanguageData.sqlite").path
        val mainDb =
            try {
                SQLiteDatabase.openDatabase(
                    dbPath,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )
            } catch (e: Exception) {
                Log.e("VerbConjugationRepository", "Could not open $dbPath", e)
                return null
            }
        val verbRow: Map<String, String?>
        try {
            verbRow = loadVerbRowMap(mainDb, trimmed) ?: return null
        } finally {
            mainDb.close()
        }

        val contract =
            contractLoader.loadContract(dbLanguageAlias.lowercase())
                ?: return null

        val expanded = expandContractToRows(contract, verbRow)
        if (expanded.isEmpty()) return null

        return VerbConjugationSession.create(expanded)
    }

    private fun loadVerbRowMap(
        db: SQLiteDatabase,
        lemma: String,
    ): Map<String, String?>? {
        val tableInfo =
            db.rawQuery("PRAGMA table_info(verbs)", null).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(c.getString(1))
                    }
                }
            }
        if (tableInfo.isEmpty()) return null

        val searchColumns =
            listOf("infinitive", "verb", "activeInfinitive").filter { it in tableInfo }
        for (col in searchColumns) {
            db.rawQuery(
                "SELECT * FROM verbs WHERE $col = ? COLLATE NOCASE LIMIT 1",
                arrayOf(lemma),
            ).use { c ->
                if (c.moveToFirst()) {
                    return cursorRowToMap(c)
                }
            }
        }
        return null
    }

    private fun cursorRowToMap(c: android.database.Cursor): Map<String, String?> {
        val names = c.columnNames
        val out = LinkedHashMap<String, String?>()
        for (name in names) {
            val idx = c.getColumnIndex(name)
            out[name] =
                if (idx < 0 || c.isNull(idx)) {
                    null
                } else {
                    c.getString(idx)
                }
        }
        return out
    }

    private fun expandContractToRows(
        contract: DataContract,
        verbRow: Map<String, String?>,
    ): List<VerbConjugationRow> {
        val sorted =
            TreeMap<String, Conjugation> { a, b ->
                val na = a.toIntOrNull()
                val nb = b.toIntOrNull()
                when {
                    na != null && nb != null -> na.compareTo(nb)
                    else -> a.compareTo(b)
                }
            }
        sorted.putAll(contract.conjugations)

        val rows = mutableListOf<VerbConjugationRow>()
        for ((_, conj) in sorted) {
            val tense = conj.title
            if (tense.isBlank()) continue
            for (slot in conj.personSlots()) {
                slot?.forEach { (personLabel, columnSpec) ->
                    val raw = formFromColumnSpec(verbRow, columnSpec)
                    val display = raw.ifEmpty { VerbConjugationRow.MISSING_FORM_PLACEHOLDER }
                    val label = personLabel.ifBlank { "—" }
                    rows.add(
                        VerbConjugationRow(
                            tense = tense,
                            personLabel = label,
                            form = display,
                        ),
                    )
                }
            }
        }
        return rows
    }

    /**
     * Resolves a contract value to display text. Supports:
     * - Single DB column name: [verbRow] lookup (with aliases for common Wikidata / Scribe exports).
     * - Space-separated parts: each token is a column name, `[columnName]`, or a quoted literal
     *   (`'` / Unicode smart quotes), e.g. `'had' pastParticiple` for English pluperfect.
     *   Non-empty parts are joined with a single space (e.g. perfect tenses, German aux + participle).
     */
    private fun formFromColumnSpec(
        verbRow: Map<String, String?>,
        spec: String,
    ): String {
        val trimmed = spec.trim()
        if (trimmed.isEmpty()) return ""
        val tokens = trimmed.split(Regex("\\s+"))
        val parts = ArrayList<String>(tokens.size)
        for (token in tokens) {
            if (token.isEmpty()) continue
            val literal = unquotedLiteralToken(token)
            val value =
                if (literal != null) {
                    literal
                } else if (token.length >= 2 && token.startsWith("[") && token.endsWith("]")) {
                    val column = token.substring(1, token.length - 1)
                    columnValueFromVerbRow(verbRow, column)
                } else {
                    columnValueFromVerbRow(verbRow, token)
                }
            if (value.isNotEmpty()) parts.add(value)
        }
        return parts.joinToString(" ")
    }

    private fun unquotedLiteralToken(token: String): String? {
        if (token.length < 2) return null
        val open = token.first()
        val close = token.last()
        val quoted =
            open == '\'' && close == '\'' ||
                open == '\u2018' && close == '\u2019' ||
                open == '\u201C' && close == '\u201D'
        if (!quoted) return null
        return token.substring(1, token.length - 1).trim()
    }

    /**
     * Maps logical contract keys to whichever column exists on this verb row (exports vary by language).
     */
    private fun columnValueFromVerbRow(
        verbRow: Map<String, String?>,
        column: String,
    ): String {
        fun firstNonEmpty(keys: Sequence<String>): String {
            for (k in keys) {
                val v = verbRow[k]?.trim().orEmpty()
                if (v.isNotEmpty()) return v
            }
            return ""
        }
        return when (column) {
            "pastParticiple" ->
                firstNonEmpty(
                    sequenceOf(
                        "pastParticiple",
                        "perfectParticiple",
                        "past",
                    ),
                )
            "infinitive" ->
                firstNonEmpty(
                    sequenceOf(
                        "infinitive",
                        "activeInfinitive",
                        "verb",
                    ),
                )
            "auxiliaryVerb" ->
                firstNonEmpty(
                    sequenceOf(
                        "auxiliaryVerb",
                        "auxiliary",
                        "perfectAuxiliary",
                        "presentPerfectAuxiliary",
                    ),
                )
            else -> verbRow[column]?.trim().orEmpty()
        }
    }

    private fun Conjugation.personSlots(): List<Map<String, String>?> =
        listOf(
            firstPerson,
            secondPerson,
            thirdPersonSingular,
            firstPersonPlural,
            secondPersonPlural,
            thirdPersonPlural,
        )
}

data class VerbConjugationRow(
    val tense: String,
    val personLabel: String,
    val form: String,
) {
    fun hasCopyableForm(): Boolean =
        form.isNotBlank() && form != MISSING_FORM_PLACEHOLDER

    companion object {
        const val MISSING_FORM_PLACEHOLDER: String = "\u2014"
    }
}

/**
 * In-memory SQLite with [verb_forms(tense, person_label, form)] for DISTINCT and filtered queries.
 */
class VerbConjugationSession private constructor(
    private val memoryDb: SQLiteDatabase,
) : Closeable {
    companion object {
        private const val TABLE = "verb_forms"

        fun create(rows: List<VerbConjugationRow>): VerbConjugationSession {
            val db = SQLiteDatabase.create(null)
            db.execSQL(
                "CREATE TABLE $TABLE (" +
                    "tense TEXT NOT NULL, " +
                    "person_label TEXT NOT NULL, " +
                    "form TEXT NOT NULL" +
                    ")",
            )
            val insert =
                db.compileStatement(
                    "INSERT INTO $TABLE (tense, person_label, form) VALUES (?,?,?)",
                )
            db.beginTransaction()
            try {
                for (r in rows) {
                    insert.clearBindings()
                    insert.bindString(1, r.tense)
                    insert.bindString(2, r.personLabel)
                    insert.bindString(3, r.form)
                    insert.executeInsert()
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            insert.close()
            return VerbConjugationSession(db)
        }
    }

    fun distinctTenses(): List<String> {
        val out = mutableListOf<String>()
        memoryDb.rawQuery(
            "SELECT DISTINCT tense FROM $TABLE ORDER BY tense COLLATE NOCASE",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                out.add(c.getString(0))
            }
        }
        return out
    }

    /**
     * @param tense null or empty means all rows (no WHERE). Otherwise [WHERE tense = ?].
     */
    fun queryRows(tense: String?): List<VerbConjugationRow> {
        val sql =
            if (tense.isNullOrEmpty()) {
                "SELECT tense, person_label, form FROM $TABLE ORDER BY tense COLLATE NOCASE, person_label COLLATE NOCASE"
            } else {
                "SELECT tense, person_label, form FROM $TABLE WHERE tense = ? ORDER BY person_label COLLATE NOCASE"
            }
        val args = if (tense.isNullOrEmpty()) null else arrayOf(tense)
        val out = mutableListOf<VerbConjugationRow>()
        memoryDb.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                out.add(
                    VerbConjugationRow(
                        tense = c.getString(0),
                        personLabel = c.getString(1),
                        form = c.getString(2),
                    ),
                )
            }
        }
        return out
    }

    override fun close() {
        memoryDb.close()
    }
}
