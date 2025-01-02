package be.scri.navigation

sealed class Screen(
    val route: String,
) {
    data object Installation : Screen("installation_screen")

    data object Settings : Screen("settings_screen")

    data object LanguageSettings : Screen("language_settings_screen")

    data object About : Screen("about_screen")

    data object PrivacyPolicy : Screen("privacy_policy_screen")

    data object WikimediaScribe : Screen("wikimedia_scribe_screen")

    data object ThirdParty : Screen("third_party_screen")
}
