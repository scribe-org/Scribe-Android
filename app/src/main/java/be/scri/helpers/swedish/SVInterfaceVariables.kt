// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers.swedish

/**
 * Interface variables for Swedish language keyboards.
 */
object SVInterfaceVariables {
    // MARK: Currency Symbols

    const val CURRENCY_SYMBOL = "kr"
    val CURRENCY_SYMBOL_ALTERNATES = listOf("kr", "$", "€", "£", "¥")

    // MARK: Keyboard Labels

    const val SPACE_BAR = "mellanslag"
    const val LANGUAGE = "Svenska"

    const val INVALID_COMMAND_MSG_WIKIDATA = "Inte i Wikidata"
    const val INVALID_COMMAND_TEXT_WIKIDATA_1 =
        "Wikidata är en gemensamt redigerad kunskapsgraf som underhålls av Wikimedia Foundation. Det fungerar som en källa till öppen data för projekt som Wikipedia och flera andra."
    const val INVALID_COMMAND_TEXT_WIKIDATA_2 =
        "Scribe använder Wikidatas språkdata för många av sina kärnfunktioner. Vi får information som substantiv, genus, verbböjningar och mycket mer!"
    const val INVALID_COMMAND_TEXT_WIKIDATA_3 =
        "Du kan skapa ett konto på wikidata.org för att gå med i communityn som stöder Scribe och så många andra projekt. Hjälp oss att ge gratis information till världen!"

    const val INVALID_COMMAND_MSG_WIKTIONARY = "Inte i Wiktionary"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_1 =
        "Wiktionary är en gemensamt redigerad ordbok som underhålls av Wikimedia Foundation. Den fungerar som en källa till fri språkdata för projekt som Wikipedia och otaliga andra."
    const val INVALID_COMMAND_TEXT_WIKTIONARY_2 =
        "Scribe använder Wiktionarys data för att tillhandahålla översättningar för sitt översättningskommando. Våra data härleds från de många språkpar som Wiktionarys gemenskap har skapat!"
    const val INVALID_COMMAND_TEXT_WIKTIONARY_3 =
        "Du kan skapa ett konto på wiktionary.org för att gå med i gemenskapen som stöder Scribe och många andra projekt. Hjälp oss att ge världen fri information!"

    val BASE_AUTOSUGGESTIONS = listOf("jag", "det", "men")
    val NUMERIC_AUTOSUGGESTIONS = listOf("jag", "det", "och")

    // MARK: Translate Command

    const val TRANSLATE_KEY_LBL = "Översätt"
    const val TRANSLATE_PLACEHOLDER = "Ange ett ord"
    const val TRANSLATE_PROMPT = "sv -› targetLanguage()"
    const val TRANSLATE_PROMPT_AND_CURSOR = TRANSLATE_PROMPT + "COMMAND_CURSOR"
    const val TRANSLATE_PROMPT_AND_PLACEHOLDER = TRANSLATE_PROMPT_AND_CURSOR + "$TRANSLATE_PLACEHOLDER"

    // MARK: Conjugate Command

    const val CONJUGATE_KEY_LBL = "Konjugera"
    const val CONJUGATE_PLACEHOLDER = "Ange ett verb"
    const val CONJUGATE_PROMPT = "Konjugera: "
    const val CONJUGATE_PROMPT_AND_CURSOR = CONJUGATE_PROMPT + "COMMAND_CURSOR"
    const val CONJUGATE_PROMPT_AND_PLACEHOLDER = CONJUGATE_PROMPT_AND_CURSOR + "$CONJUGATE_PLACEHOLDER"

    // MARK: Plural Command

    const val PLURAL_KEY_LBL = "Plural"
    const val PLURAL_PLACEHOLDER = "Ange ett substantiv"
    const val PLURAL_PROMPT = "Plural: "
    const val PLURAL_PROMPT_AND_CURSOR = PLURAL_PROMPT + "COMMAND_CURSOR"
    const val PLURAL_PROMPT_AND_PLACEHOLDER = PLURAL_PROMPT_AND_CURSOR + "$PLURAL_PLACEHOLDER"
    const val ALREADY_PLURAL_MSG = "Redan plural"
}
