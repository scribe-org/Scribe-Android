# complete code
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

class OutreachSocialMediaTest {
    @Test
    fun testPostOnSocialMedia() {
        val context = Application()
        val outreachSocialMedia = OutreachSocialMedia(context)
        outreachSocialMedia.postOnSocialMedia()
        verify(outreachSocialMedia).postOnSocialMedia()
    }
}