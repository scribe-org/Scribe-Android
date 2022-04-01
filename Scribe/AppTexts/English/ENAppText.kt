/**
 * ENAppText.kt
 *
 * The English app text for the Scribe app.
 */

/**
 * Formats and returns the title of the installation guidelines given a [fontSize] based on screen dimensions.
 */
internal fun getENInstallationTitle(fontSize: CGFloat) : NSMutableAttributedString =
    NSMutableAttributedString(
        string = "Keyboard Installation",
        attributes = mapOf<NSAttributedString.Key.font , UIFont.boldSystemFont(ofSize = fontSize * 1.5)>
    )

/**
 * Formats and returns the directions of the installation guidelines given a [fontSize] based on screen dimensions.
 */
internal fun getENInstallationDirections(fontSize: CGFloat) : NSMutableAttributedString {
    val arrowString = getArrowIcon(fontSize = fontSize)
    val globeString = getGlobeIcon(fontSize = fontSize)
    val startOfBody = NSMutableAttributedString(string = """
\n
1.\u0020
""", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>)
    val settingsLink = addHyperLinks(
        originalText = "Open Settings",
        links = mapOf<"Open Settings" , "<makeTextLink>">, // placeholder as there's a button over it
        fontSize = fontSize
    )
    val installStart = concatAttributedStrings(left = startOfBody, right = settingsLink)
    val installDirections = NSMutableAttributedString(string = """
\n
2. In General do the following:

        Keyboard

""", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>)
    installDirections.append(NSAttributedString(string = "\n         "))
    installDirections.append(arrowString)
    installDirections.append(NSMutableAttributedString(string = """
\u0020 Keyboards

""", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>))
    installDirections.append(NSMutableAttributedString(string = "\n                    ", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>))
    installDirections.append(arrowString)
    installDirections.append(NSMutableAttributedString(string = """
\u0020 Add New Keyboard

3. Select Scribe and then activate keyboards

4. When typing press\u0020
""", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>))
    installDirections.append(globeString)
    installDirections.append(NSMutableAttributedString(string = """
\u0020to select keyboards
""", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>))
    return concatAttributedStrings(left = installStart, right = installDirections)
}

/**
 * Formats and returns the full text for the installation guidelines given a [fontSize] based on screen dimensions.
 */
internal fun setENInstallation(fontSize: CGFloat) : NSMutableAttributedString {
    val installTitle = getENInstallationTitle(fontSize = fontSize)
    val installDirections = getENInstallationDirections(fontSize = fontSize)
    return concatAttributedStrings(left = installTitle, right = installDirections)
}

/**
 * Formats and returns the title of the GitHub information given a [fontSize] based on screen dimensions.
 */
internal fun getENGitHubTitle(fontSize: CGFloat) : NSMutableAttributedString =
    NSMutableAttributedString(string = """
Community
  """, attributes = mapOf<NSAttributedString.Key.font , UIFont.boldSystemFont(ofSize = fontSize * 1.5)>)

/**
 * Formats and returns the text of the GitHub information given a [fontSize] based on screen dimensions.
 */
internal fun getENGitHubText(fontSize: CGFloat) : NSMutableAttributedString {
    // Initialize the main body of the text.
    val ghInfoText = NSMutableAttributedString(string = """
\n
Scribe is fully open-source. To report issues or contribute please visit us at\u0020
""", attributes = mapOf<NSAttributedString.Key.font , UIFont.systemFont(ofSize = fontSize)>)
    // A second NSAttributedString that includes a link to the GitHub.
    val ghLink = addHyperLinks(
        originalText = "github.com/scribe-org.",
        links = mapOf<"github.com/scribe-org" , "https://github.com/scribe-org">,
        fontSize = fontSize
    )
    return concatAttributedStrings(left = ghInfoText, right = ghLink)
}

/**
 * Formats and returns the text for a notice about Scribe's GitHub given a [fontSize] based on screen dimensions.
 */
internal fun setENGitHubText(fontSize: CGFloat) : NSMutableAttributedString {
    val ghTextTitle = getENGitHubTitle(fontSize = fontSize)
    val ghInfoTextAndLink = getENGitHubText(fontSize = fontSize)
    return concatAttributedStrings(left = ghTextTitle, right = ghInfoTextAndLink)
}
