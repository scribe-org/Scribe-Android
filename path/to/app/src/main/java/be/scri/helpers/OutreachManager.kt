# complete code
import androidx.core.util.*

class OutreachManager(private val context: Context) {
    private val constants: Constants = Constants(context)

    fun postOnSocialMedia() {
        val url = constants.getOutreachSocialMediaUrl()
        // Implement social media posting logic here
        println("Posting on social media: $url")
    }

    fun postOnForums() {
        val url = constants.getOutreachForumUrl()
        // Implement forum posting logic here
        println("Posting on forums: $url")
    }

    fun postOnLanguageInstitutions() {
        val url = constants.getOutreachLanguageInstitutionsUrl()
        // Implement language institution posting logic here
        println("Posting on language institutions: $url")
    }
}