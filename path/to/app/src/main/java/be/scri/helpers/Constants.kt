# complete code
import androidx.core.util.*

class Constants(private val context: Context) {
    companion object {
        const val OUTREACH_SOCIAL_MEDIA_URL = "https://wikis.world/@scribe"
        const val OUTREACH_FORUM_URL = "https://news.ycombinator.com/"
        const val OUTREACH_LANGUAGE_INSTITUTIONS_URL = "https://www.goethe.de/de/index.html"
    }

    fun getOutreachSocialMediaUrl(): String {
        return OUTREACH_SOCIAL_MEDIA_URL
    }

    fun getOutreachForumUrl(): String {
        return OUTREACH_FORUM_URL
    }

    fun getOutreachLanguageInstitutionsUrl(): String {
        return OUTREACH_LANGUAGE_INSTITUTIONS_URL
    }
}