# complete code
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class OutreachManagerTest {
    @Test
    fun testPostOnSocialMedia() {
        val context = Application()
        val constants = Constants(context)
        val outreachManager = OutreachManager(context)
        val socialMediaUrl = constants.getOutreachSocialMediaUrl()
        outreachManager.postOnSocialMedia()
        verify(outreachManager).postOnSocialMedia()
    }

    @Test
    fun testPostOnForums() {
        val context = Application()
        val constants = Constants(context)
        val outreachManager = OutreachManager(context)
        val forumUrl = constants.getOutreachForumUrl()
        outreachManager.postOnForums()
        verify(outreachManager).postOnForums()
    }

    @Test
    fun testPostOnLanguageInstitutions() {
        val context = Application()
        val constants = Constants(context)
        val outreachManager = OutreachManager(context)
        val languageInstitutionsUrl = constants.getOutreachLanguageInstitutionsUrl()
        outreachManager.postOnLanguageInstitutions()
        verify(outreachManager).postOnLanguageInstitutions()
    }
}