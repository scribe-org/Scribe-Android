# complete code
import org.junit.Assert.*

class ConstantsTest {
    @Test
    fun testGetOutreachSocialMediaUrl() {
        val context = Application()
        val constants = Constants(context)
        assertEquals(Constants.OUTREACH_SOCIAL_MEDIA_URL, constants.getOutreachSocialMediaUrl())
    }

    @Test
    fun testGetOutreachForumUrl() {
        val context = Application()
        val constants = Constants(context)
        assertEquals(Constants.OUTREACH_FORUM_URL, constants.getOutreachForumUrl())
    }

    @Test
    fun testGetOutreachLanguageInstitutionsUrl() {
        val context = Application()
        val constants = Constants(context)
        assertEquals(Constants.OUTREACH_LANGUAGE_INSTITUTIONS_URL, constants.getOutreachLanguageInstitutionsUrl())
    }
}