# complete code
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class OutreachLanguageInstitutionsTest {
    @Test
    fun testPostOnLanguageInstitutions() {
        val context = Application()
        val outreachLanguageInstitutions = OutreachLanguageInstitutions(context)
        outreachLanguageInstitutions.postOnLanguageInstitutions()
        verify(outreachLanguageInstitutions).postOnLanguageInstitutions()
    }
}